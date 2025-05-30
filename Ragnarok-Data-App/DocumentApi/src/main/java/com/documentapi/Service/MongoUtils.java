/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.documentapi.Service;


import com.documentapi.Configuration.MongoConfig;
import com.documentapi.Exception.DocumentNotFoundException;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
     private final String PARAM_DESIGNATION_VALUE = "akt-plné-označení";
     private final MongoDatabase database;
     private final MongoConfig config;

     private final IdFetcherService idFetcher;
     @Autowired
     ObjectMapper objectMapper;
     public MongoUtils(MongoDatabase database,IdFetcherService idFetcher,MongoConfig config){
         this.idFetcher = idFetcher;
         this.database = database;
         this.config = config;
        
     }
     
     
      public  boolean createCollection(String collectionName) {
        try {
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
    
    
    
    public JsonNode getDocumentByDesignation(String designation) throws JsonProcessingException, DocumentNotFoundException{
        Document doc = getMongoCollection(config.MONGO_COLLECTION_AKTY_FINAL).find(Filters.eq("akt-plné-označení", designation)).first();
        return objectMapper.readTree(appendLinks(appendRelations(doc)).toJson());
    }
    
    public JsonNode getRelatedDocuments(String designation)  throws JsonProcessingException, DocumentNotFoundException {
        Document baseDoc = getMongoCollection(config.MONGO_COLLECTION_AKTY_FINAL)
            .find(Filters.eq(PARAM_DESIGNATION_VALUE, designation))
            .first();

        if (baseDoc == null) {
            return new ObjectMapper().createArrayNode();
        }

        Integer zneniBaseId = baseDoc.getInteger("znění-base-id");
        if (zneniBaseId == null) {
            return new ObjectMapper().createArrayNode();
        }

        List<Document> allDocs = getMongoCollection(config.MONGO_COLLECTION_AKTY_ZNENI)
                .find(Filters.eq("znění-base-id", zneniBaseId))
                .into(new ArrayList<>());

        System.out.println("Getting related documents. Got " + allDocs.size() + " versions of current act");
        Set<String> parsedIriSet = new LinkedHashSet<>(); 

        for (Document doc : allDocs) {
            List<Document> vazbaAktList = doc.getList("znění-vazba-akt", Document.class);
            if (vazbaAktList != null) {
                for (Document vazba : vazbaAktList) {
                    Document pravniAktZneni = vazba.get("právní-akt-znění", Document.class);
                    if (pravniAktZneni != null) {
                        String iri = pravniAktZneni.getString("iri");
                        if (iri != null) {
                            String transformed = transformIri(iri);
                            parsedIriSet.add(transformed);
                        }
                    }
                }
            }
        }

        
        System.out.println("Getting related documents. Got " + parsedIriSet.size() + " related documents" );
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(new ArrayList<>(parsedIriSet));
    }
    
    
    private String transformIri(String iri) {
        String[] parts = iri.split("/");
        List<String> numeric = new ArrayList<>();
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].matches("\\d+")) {
                numeric.add(parts[i]);
                if (numeric.size() == 2) {
                    break;
                }
            }
        }
        if (numeric.size() < 2) {
            return iri;
        }
        return numeric.get(0) + "/" + numeric.get(1) + " Sb.";
    }
    

    
    
    public JsonNode getMongoCollectionAsJson(String collectionName) throws JsonProcessingException {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Document doc : getMongoCollection(collectionName).find()) {
            JsonNode jsonNode = objectMapper.readTree(doc.toJson());
            arrayNode.add(jsonNode); 
        }
        return arrayNode;
    }
    
    public JsonNode getDocumentByID(Integer id) throws JsonProcessingException, DocumentNotFoundException{
        Document doc = getMongoCollection(config.MONGO_COLLECTION_AKTY_FINAL).find(Filters.eq("znění-dokument-id", id)).first();
        return objectMapper.readTree(appendLinks(appendRelations(doc)).toJson());
    }
    
    
    
    private Document appendLinks(Document doc) throws DocumentNotFoundException{
        if(doc == null){
            throw new DocumentNotFoundException("Document not found");
        }
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
    
    
    
    private Document appendRelations(Document doc) throws DocumentNotFoundException {
        if (doc == null) {
            throw new DocumentNotFoundException("Document not found");
        }
        String zneniDokumentId = doc.getString("akt-plné-označení");
        if (zneniDokumentId == null) {
            return doc;
        }

        try {
            JsonNode relatedDocsNode = getRelatedDocuments(zneniDokumentId);
            List<String> relatedDocsList = objectMapper.convertValue(
                relatedDocsNode,
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );
            doc.append("vztažené-akty", relatedDocsList);
            return doc;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return doc;
        }
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
    
    
     public JsonNode getMetadataByLink(String link) throws DocumentNotFoundException, JsonProcessingException {
        MongoCollection<Document> collection = getMongoCollection(config.MONGO_COLLECTION_AKTY_FINAL);

        Document doc = collection.find(new Document("odkaz-stažení-pdf", link)).first();
        
        if (doc == null) {
            doc = collection.find(new Document("odkaz-stažení-docx", link)).first();
        }

        if (doc == null) {
            throw new DocumentNotFoundException("Document not found by either 'odkaz-stažení-pdf' or 'odkaz-stažení-docx'");
        }
        return objectMapper.readTree(appendLinks(appendRelations(doc)).toJson());
    }
     
     public Integer getCollectionSize(String collection) {
        return (int) getMongoCollection(collection).countDocuments();
    }
     
     
    public String getDocxUrl(String designation) throws JsonProcessingException, DocumentNotFoundException{
        JsonNode node = getDocumentByDesignation(designation);
        String url = node.get("odkaz-stažení-docx").asText();
       if(url == null){
           url =  node.get("odkaz-stažení-pdf").asText();
       }
       return url;
    }
    
    
     public String getPdfUrl(String designation) throws JsonProcessingException, DocumentNotFoundException{
        JsonNode node = getDocumentByDesignation(designation);
        String url = node.get("odkaz-stažení-pdf").asText();
       if(url == null){
           url =  node.get("odkaz-stažení-docx").asText();
       }
       return url;
    }
}
