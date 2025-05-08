/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Service;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

     public MongoUtils(MongoDatabase database){
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
    
       
     public Integer getCollectionSize(String collection) {
        return (int) getMongoCollection(collection).countDocuments();
    }
     
     
    public Set<Integer> getListOfUniqueZnenBaseIDsForCollection(MongoCollection<Document> sourceCollection){
        Set<Integer> uniqueIds = new HashSet<>();
        try (MongoCursor<Document> cursor = sourceCollection.find().iterator()) {
            int idCount = 0;
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Integer dokumentId = doc.getInteger("znění-base-id");
                if (dokumentId != null) {
                    uniqueIds.add(dokumentId);
                    idCount++;
                }
            }
        }
        
        return uniqueIds;
    }
        
        
     public Set<Integer> getListOfUniqueZnenDokumentIDsForCollection(MongoCollection<Document> sourceCollection){
        Set<Integer> uniqueIds = new HashSet<>();
        try (MongoCursor<Document> cursor = sourceCollection.find().iterator()) {
            int idCount = 0;
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Integer dokumentId = doc.getInteger("znění-dokument-id");
                if (dokumentId != null) {
                    uniqueIds.add(dokumentId);
                    idCount++;
                }
            }
        }
        
        return uniqueIds;
    }
       
}
