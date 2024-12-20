/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author brune
 */
@Service
@EnableAsync
public class MongoUtils {
     private static final Logger logger = LoggerFactory.getLogger(MongoUtils.class);
     private final MongoDatabase database;

     
     private boolean isProcessing = false;

     private final IdFetcherService idFetcher;
     @Autowired
     ObjectMapper objectMapper;
     public MongoUtils(MongoDatabase database,IdFetcherService idFetcher){
         this.idFetcher = idFetcher;
         this.database = database;
        
     }
     
    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
    }
     
     
      public  boolean createCollection(String collectionName) {
        try {
            // Check if the collection already exists
             dropCollectionIfExists(collectionName);
            logger.info("Collection '{}' created successfully.", collectionName);
            return true;
        } catch (MongoCommandException e) {
            logger.error("Failed to create collection '{}'. Error: {}", collectionName, e.getErrorMessage());
            return false;
        } catch (Exception e) {
            logger.error("An unexpected error occurred while creating collection '{}'.", collectionName, e);
            return false;
        }
    }
      
      
      
      public Document findInCollection(MongoCollection<Document> collection, String key, Integer value){
             Document filter = new Document(key, value);
            
            // Retrieve the document
            Document document = collection.find(filter).first();
            
            if (document == null) {
                logger.warn("No document found with {} {}",key, value);
                return null;
            }
            return document;
      }
      
      
       private boolean dropCollectionIfExists(String collectionName) {
        try {
            MongoIterable<String> collectionNames = database.listCollectionNames();
            boolean exists = false;
            for (String name : collectionNames) {
                if (name.equalsIgnoreCase(collectionName)) {
                    exists = true;
                    break;
                }
            }
            
            if (exists) {
                database.getCollection(collectionName).drop();
                logger.info("Collection '{}' has been dropped.", collectionName);
            } else {
                logger.info("Collection '{}' does not exist. No need to drop.", collectionName);
            }
            return true;
        } catch (Exception e) {
            logger.error("An error occurred while dropping collection '{}'. Error: {}", collectionName, e.getMessage());
            return false;
        }
    }
       
    public MongoCollection<Document> getMongoCollection(String collectionName){
        return database.getCollection(collectionName);
    }
    
    
    
    public JsonNode getDocumentByDesignation(String collectionName, String designation) throws JsonProcessingException{
        Document doc = getMongoCollection(collectionName).find(Filters.eq("akt-plné-označení", designation)).first();
        return objectMapper.readTree(appendLinks(doc).toJson());
    }
    

    
    
    public JsonNode getMongoCollectionAsJson(String collectionName) throws JsonProcessingException {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Document doc : getMongoCollection(collectionName).find()) {
            JsonNode jsonNode = objectMapper.readTree(doc.toJson());
            arrayNode.add(jsonNode); 
        }
        return arrayNode;
    }
    
    public JsonNode getDocumentByID(String collectionName, Integer id) throws JsonProcessingException{
        Document doc = getMongoCollection(collectionName).find(Filters.eq("znění-dokument-id", id)).first();
        return objectMapper.readTree(appendLinks(doc).toJson());
    }
    
    
    
    private Document appendLinks(Document doc){
        String pdfId = idFetcher.fetchIdWithRetry(doc.getInteger("znění-dokument-id"), "PDF");
        String docxId = idFetcher.fetchIdWithRetry(doc.getInteger("znění-dokument-id"), "DOCX");
        if (pdfId != null) {
            doc.append("odkaz-stažení-pdf", "https://www.e-sbirka.cz/souborove-sluzby/soubory/" + pdfId);
        }
        else{
             doc.append("odkaz-stažení-pdf", null);
        }
        if (docxId != null) {
            doc.append("odkaz-stažení-docx", "https://www.e-sbirka.cz/souborove-sluzby/soubory/" + docxId);
        }
        else{
           doc.append("odkaz-stažení-docx", null);  
        }
        return doc;
    }
    
     public JsonNode getLinksFromCollection(String collectionName) throws JsonProcessingException{
           ArrayNode arrayNode = objectMapper.createArrayNode();
           for (Document doc : getMongoCollection(collectionName).find()) {     
                String pdfLink = doc.getString("odkaz-stažení-pdf");
                String docxLink = doc.getString("odkaz-stažení-docx");               
                ObjectNode newNode = objectMapper.createObjectNode();
                newNode.put("pdf", pdfLink);
                newNode.put("docx", docxLink);
                arrayNode.add(newNode);
               
            }
           return arrayNode;
    }
     
    public JsonNode getAllWithinRange(String collectionName,int from,int to) throws JsonProcessingException, IllegalArgumentException{
        if (from < 0 || to < from) {
           throw new IllegalArgumentException("Invalid 'from' or 'to' parameters");
       }
        int limit = to - from +1;
          ArrayNode arrayNode = objectMapper.createArrayNode();
         for (Document doc : getMongoCollection(collectionName).find().skip(from - 1).limit(limit)) {
            arrayNode.add(objectMapper.readTree(doc.toJson()));
         }
         return arrayNode;
    }
    
    /*
    public JsonNode getOneByID(String collectionName,int id){
        
    }
    */
    
    
     public JsonNode getMetadataByLink(String collectionName,String link) throws Exception{
        MongoCollection<Document> collection = getMongoCollection(collectionName);

        Document doc = collection.find(new Document("odkaz-stažení-pdf", link)).first();
        
        if (doc == null) {
            doc = collection.find(new Document("odkaz-stažení-docx", link)).first();
        }

        if (doc == null) {
            throw new Exception("Document not found by either 'odkaz-stažení-pdf' or 'odkaz-stažení-docx'");
        }
        return objectMapper.readTree(doc.toJson());
    }
     
     public Integer getCollectionSize(String collection) {
        return (int) getMongoCollection(collection).countDocuments();
    }
                  
    
       
}
