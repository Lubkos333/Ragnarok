/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Service;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongoiswriter.Configuration.MongoConfig;
import com.mongoiswriter.Enum.ExtracterType;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
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
public class JsonExtracterUtil {
       private static final Logger logger = LoggerFactory.getLogger(JsonExtracterUtil.class);
       public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
       private final MongoDatabase database;
       private final MongoUtils mongoUtils;
       private final ObjectMapper objectMapper;
       private final MongoConfig mongoConfig;
       
       private Set<Integer> uniqueIds;
              
       public JsonExtracterUtil(MongoDatabase database,MongoUtils mongoUtils, ObjectMapper objectMapper, MongoConfig mongoConfig){
           this.database = database;
           this.mongoUtils = mongoUtils;
           this.objectMapper = objectMapper;
           this.mongoConfig = mongoConfig;
       }

    public void extractFromAddressToMongo(String sourceURL, String collectionName, ExtracterType extracterType)
        throws MalformedURLException, SocketException, IOException {
            JsonFactory jsonFactory = new JsonFactory();
            mongoUtils.createCollection(collectionName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            collection = collection.withWriteConcern(WriteConcern.ACKNOWLEDGED);

            uniqueIds = mongoUtils.getListOfUniqueZnenDokumentIDsForCollection(
                mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL)
        );

        URL url = new URL(sourceURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(6000000); 
        connection.setReadTimeout(60000000); 
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Range", "bytes=0-");

        try (InputStream urlInputStream = connection.getInputStream();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(urlInputStream, 100 * 1024);
             GZIPInputStream gzipInputStream = new GZIPInputStream(bufferedInputStream);
             JsonParser jsonParser = jsonFactory.createParser(gzipInputStream)) {

            JsonToken token = jsonParser.nextToken();

            if (token == JsonToken.START_OBJECT) {
                processRootObject(jsonParser, collection, extracterType);
            } else {
                throw new IOException("Expected data to start with an Object");
            }
            
             if (extracterType == ExtracterType.PRAVNI_AKT) {
                collection.createIndex(Indexes.ascending("znění-dokument-id"));
            }

            System.out.println("Source size: " + mongoUtils.getCollectionSize(collectionName));
            System.out.println("Import completed successfully.");
        } catch (IOException e) {
            System.out.println("Source size: " + mongoUtils.getCollectionSize(collectionName));
            System.err.println("IOException occurred: " + e.getMessage());
            mongoUtils.setProcessing(false);
            e.printStackTrace();
            throw new SocketException();
        } catch (MongoException me) {
            System.err.println("MongoException occurred: " + me.getMessage());
            me.printStackTrace();
        }
    }

    private void processRootObject(JsonParser jsonParser, MongoCollection<Document> collection, ExtracterType extracterType) throws IOException {
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            jsonParser.nextToken();

            if ("položky".equals(fieldName) && jsonParser.currentToken() == JsonToken.START_ARRAY) {
                processItemsArray(jsonParser, collection, extracterType);
            } else {
                jsonParser.skipChildren();
            }
        }
    }

    private void processItemsArray(JsonParser jsonParser, MongoCollection<Document> collection, ExtracterType extracterType) throws IOException {
        int processedNumber = 0;
        int batchSize = 1000; 
        List<Document> batch = new ArrayList<>();

        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                Document doc = parseSingleDocument(jsonParser, objectMapper, extracterType);
                if (doc != null) {
                    batch.add(doc);
                }

                processedNumber++;

                if (batch.size() >= batchSize) {
                    collection.insertMany(batch);
                    batch.clear();
                    System.out.println("Inserted " + processedNumber + " documents so far...");
                }
            } else {
                jsonParser.skipChildren();
            }
        }

        if (!batch.isEmpty()) {
            collection.insertMany(batch);
            System.out.println("Final insert of remaining " + batch.size() + " documents.");
        }

        System.out.println("Processed total documents: " + processedNumber);
    }
    
    
    

    private Document parseSingleDocument(JsonParser jsonParser, ObjectMapper objectMapper, ExtracterType extracterType) throws IOException {
       JsonNode jsonNode = objectMapper.readTree(jsonParser);
       
       String jsonString = objectMapper.writeValueAsString(jsonNode);
       Document document = Document.parse(jsonString);
       
       if (extracterType == ExtracterType.PRAVNI_AKT) {
           JsonNode zneniDatumUcinostiDoNode = jsonNode.get("znění-datum-účinnosti-do");
           JsonNode zneniDatumUcinostiOdNode = jsonNode.get("znění-datum-účinnosti-od");
           JsonNode metadataDatumZruseniNode = jsonNode.get("metadata-datum-zrušení");
           LocalDate currentDate = LocalDate.now();

           if (zneniDatumUcinostiOdNode != null) {
               LocalDate zneniDatumUcinostiOd = LocalDate.parse(zneniDatumUcinostiOdNode.asText(), formatter);
               if (zneniDatumUcinostiOd.isAfter(currentDate)) {

                  return null;
               }
           }

           if (zneniDatumUcinostiDoNode != null && !zneniDatumUcinostiDoNode.isNull()) {
               LocalDate zneniDatumUcinostiDo = LocalDate.parse(zneniDatumUcinostiDoNode.asText(), formatter);
               if (zneniDatumUcinostiDo.isBefore(currentDate)) {

                   document.append("status", "NEPLATNY");
               }
           }

           if (metadataDatumZruseniNode != null && !metadataDatumZruseniNode.isNull()) {
               LocalDate metadataDatumZruseni = LocalDate.parse(metadataDatumZruseniNode.asText(), formatter);
               if (metadataDatumZruseni.isBefore(currentDate)) {
  
                    return null;
               }
           }
           
           
            document.append("status", "PLATNY");
                   
       } 
       return document; 
   }
       
       
   public static String getValueFromField(Document document, String fieldName, String key){
       
         List<Document> cvsTerminList = (List<Document>) document.get(fieldName);
            
            if (cvsTerminList == null || cvsTerminList.isEmpty()) {
                logger.warn("'cvs-termín' field is missing or empty for definice-termínu-id");
                return null;
            }                       
       
          Document cvsTerminDoc = cvsTerminList.get(0);
          return cvsTerminDoc.getString(key);
   }

    
}



