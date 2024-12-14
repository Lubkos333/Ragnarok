/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;

import Configuration.MongoConfig;
import Enum.ExtracterType;
import Model.ExtractionMetadata;
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
       private final StringParser stringParser; 
       private final ObjectMapper objectMapper;
       private final MongoConfig mongoConfig;
       
       private Set<Integer> uniqueIds;
              
       public JsonExtracterUtil(MongoDatabase database,MongoUtils mongoUtils,StringParser stringParser,ObjectMapper objectMapper, MongoConfig mongoConfig){
           this.database = database;
           this.mongoUtils = mongoUtils;
           this.stringParser = stringParser;
           this.objectMapper = objectMapper;
           this.mongoConfig = mongoConfig;
       }

    public void extractFromAddressToMongo(ExtractionMetadata extractionMetadata)
        throws MalformedURLException, SocketException, IOException {
            JsonFactory jsonFactory = new JsonFactory();
            mongoUtils.createCollection(extractionMetadata.getCollectionName());
            MongoCollection<Document> collection = database.getCollection(extractionMetadata.getSourceURL());

            collection = collection.withWriteConcern(WriteConcern.ACKNOWLEDGED);
            if(extractionMetadata.getExtracterType() == ExtracterType.PRAVNI_AKT_VAZBA){
                    uniqueIds = mongoUtils.getListOfUniqueZnenDokumentIDsForCollection(
                    mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL)
                );
            }


            try (InputStream urlInputStream = getHttpURLConnection(extractionMetadata.getSourceURL()).getInputStream();
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(urlInputStream, 100 * 1024);
                 GZIPInputStream gzipInputStream = new GZIPInputStream(bufferedInputStream);
                 JsonParser jsonParser = jsonFactory.createParser(gzipInputStream)) {

                JsonToken token = jsonParser.nextToken();

                if (token == JsonToken.START_OBJECT) {
                    processRootObject(jsonParser, collection, extractionMetadata.getExtracterType());
                } else {
                    throw new IOException("Expected data to start with an Object");
                }

                if (extractionMetadata.getExtracterType() == ExtracterType.PRAVNI_AKT_VAZBA) {
                    collection.createIndex(Indexes.ascending("znění-cíl-dokument-id"));
                    collection.createIndex(Indexes.ascending("znění-fragment-zdroj.znění-dokument-id"));
                } else if (extractionMetadata.getExtracterType() == ExtracterType.PRAVNI_AKT) {
                    collection.createIndex(Indexes.ascending("znění-dokument-id"));
                }

                System.out.println("Source size: " + mongoUtils.getCollectionSize(extractionMetadata.getCollectionName()));
                System.out.println("Import completed successfully.");
            } catch (IOException e) {
                System.out.println("Source size: " + mongoUtils.getCollectionSize(extractionMetadata.getCollectionName()));
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
                Document doc = parseSingleDocument(jsonParser, objectMapper, extracterType, uniqueIds);
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
    
    
    

    private Document parseSingleDocument(JsonParser jsonParser, ObjectMapper objectMapper, ExtracterType extracterType, Set<Integer> uniqueIds) throws IOException {
       // Read the JSON object into a tree model
       JsonNode jsonNode = objectMapper.readTree(jsonParser);

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
                   return null;
               }
           }

           if (metadataDatumZruseniNode != null && !metadataDatumZruseniNode.isNull()) {
               LocalDate metadataDatumZruseni = LocalDate.parse(metadataDatumZruseniNode.asText(), formatter);
               if (metadataDatumZruseni.isBefore(currentDate)) {
                   return null;
               }
           }
        }

       if (extracterType == ExtracterType.PRAVNI_AKT_VAZBA) {
           JsonNode zneniCilDokumentID = jsonNode.get("znění-cíl-dokument-id");
           JsonNode firstFragment = jsonNode.path("znění-fragment-cíl").path(0);
           JsonNode firstZneniDokumentID = firstFragment.path("znění-dokument-id");
           
           if (zneniCilDokumentID.asInt() == firstZneniDokumentID.asInt()) {
               return null;
           }
           if (!uniqueIds.contains(zneniCilDokumentID.asInt()) || !uniqueIds.contains(firstZneniDokumentID.asInt())) {
               return null;
           }
       }

       String jsonString = objectMapper.writeValueAsString(jsonNode);
       Document document = Document.parse(jsonString);

       return document;
   }
    
   private HttpURLConnection getHttpURLConnection(String urlToFile) throws IOException{
            URL url = new URL(urlToFile);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(6000000); 
            connection.setReadTimeout(60000000); 
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Range", "bytes=0-");
            
            return connection;
   }

    
}
