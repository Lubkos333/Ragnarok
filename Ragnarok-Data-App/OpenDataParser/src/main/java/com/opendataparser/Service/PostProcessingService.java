/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.opendataparser.Service;

import com.opendataparser.Configuration.DataSourceConfig;
import com.opendataparser.Configuration.MongoConfig;
import com.opendataparser.Configuration.ProcessConfig;
import com.opendataparser.Enum.ExtracterType;
import com.opendataparser.Model.ExtractionMetadata;
import com.opendataparser.Model.Segments;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
public class PostProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PostProcessingService.class);
    
    private static final OkHttpClient HTTP_CLIENT = createUnsafeOkHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private static final ConcurrentMap<Integer, String> PDF_ID_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, String> DOCX_ID_CACHE = new ConcurrentHashMap<>();

    private static final int MAX_FETCH_ATTEMPTS = 3;
    private static final int INITIAL_DELAY_MILLIS = 500;
    
  
    private final MongoUtils mongoUtils;
    private final StringParser stringParser;
    private final MongoConfig mongoConfig;
    private final ProcessConfig processingConfig;
    private final DataSourceConfig dataSourceConfig;
    private final JsonExtracterUtil jsonExtracterUtil;
    private final SegmentsExtractionUtil segmentsExtractionUtil;
    
    public PostProcessingService(MongoUtils mongoUtils, StringParser stringParser, MongoConfig mongoConfig, ProcessConfig processingConfig,DataSourceConfig dataSourceConfig,JsonExtracterUtil jsonExtracterUtil,SegmentsExtractionUtil segmentsExtractionUtil) {
        this.mongoUtils = mongoUtils;
        this.stringParser = stringParser;
        this.mongoConfig = mongoConfig;
        this.processingConfig = processingConfig;
        this.dataSourceConfig = dataSourceConfig;
        this.jsonExtracterUtil = jsonExtracterUtil;
        this.segmentsExtractionUtil = segmentsExtractionUtil;
    }
    
    public void setupMongo() throws MalformedURLException, SocketException, IOException {
        
            mongoUtils.setProcessing(true);   
            
            try{
                
                jsonExtracterUtil.extractFromAddressToMongo(new ExtractionMetadata(dataSourceConfig.URL_AKTY_ZNENI,mongoConfig.MONGO_COLLECTION_AKTY_ZNENI,ExtracterType.PRAVNI_AKT));
                mongoUtils.createCollection(mongoConfig.MONGO_COLLECTION_TERMINY_FINAL);
                mongoUtils.createCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL);
                
                MongoCollection<Document> terminyProcessedCollection = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_TERMINY_FINAL);
            
                MongoCollection<Document> zneniCollection = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL);
                MongoCollection<Document> zneniPravniAktCollection = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_ZNENI);
                transferNewestDocuments(zneniPravniAktCollection,zneniCollection);
                
                jsonExtracterUtil.extractFromAddressToMongo(new ExtractionMetadata(dataSourceConfig.URL_AKTY_VAZBA,mongoConfig.MONGO_COLLECTION_AKTY_VAZBA,ExtracterType.PRAVNI_AKT_VAZBA));
 
                setRelationsForCollection();
         
            }
            catch(SocketException e){
                throw new SocketException("Socket exception. Cannot open socket");
            }
            catch(MalformedURLException e){
                throw new MalformedURLException("URL exception. URL is not correct");
            }
            mongoUtils.setProcessing(false);
        
    }
    
    public void startTransfer(MongoCollection<Document> sourceCollection, MongoCollection<Document> targetCollection) throws InterruptedException {
        Set<Integer> uniqueIds = mongoUtils.getListOfUniqueZnenBaseIDsForCollection(sourceCollection);
        ExecutorService executor = Executors.newFixedThreadPool(processingConfig.THREAD_NUMBER);
        uniqueIds.forEach(dokumentId -> {
            executor.submit(() -> processDocument(dokumentId,sourceCollection,targetCollection));
        });
        executor.shutdown();
        executor.awaitTermination(6, TimeUnit.HOURS);
    }
        
    private void processDocument(Integer dokumentId,MongoCollection<Document> sourceCollection, MongoCollection<Document> targetCollection) {
        Document doc = sourceCollection.find(Filters.eq("znění-base-id", dokumentId))
                .sort(Sorts.descending("znění-datum-účinnosti-od"))
                .first();

        if (doc != null) {
            Integer zneniDokumentId = getInteger(doc, "znění-dokument-id");
            Integer zneniBaseId = getInteger(doc, "znění-base-id");
            String aktNazevVyhlasen = doc.getString("akt-název-vyhlášený");
            String cisEsbTypZneniPolozka = doc.getString("cis-esb-typ-znění-položka");
            String zneniDatumUcinnostiOdStr = doc.getString("znění-datum-účinnosti-od");
            String aktCitace = doc.getString("akt-citace");
            Segments sbirka = segmentsExtractionUtil.extractSegments(doc.getString("akt-iri"));

            Document typDoc = (Document) doc.get("cis-esb-podtyp-právní-akt");

            String podTypAktu = null;
            if (typDoc != null && typDoc.getString("iri") != null) {
                podTypAktu = stringParser.extractAfterLastSlashAsString(typDoc.getString("iri"));
            }
        
            Document newDoc = createZneniDocument(zneniDokumentId, zneniBaseId, aktNazevVyhlasen,
                    cisEsbTypZneniPolozka, zneniDatumUcinnostiOdStr,
                    sbirka.getTypSbirky(), podTypAktu, sbirka.getCisloAktu(), aktCitace);
        
            targetCollection.insertOne(newDoc);
        }
    }
    
    private void setRelationsForCollection() {
        MongoCollection<Document> collection1 = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL);
        MongoCollection<Document> collection2 = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_VAZBA);

        collection1.createIndex(Indexes.ascending("znění-dokument-id"));
        collection2.createIndex(Indexes.ascending("znění-cíl-dokument-id"));
        collection2.createIndex(Indexes.ascending("znění-fragment-zdroj.znění-dokument-id"));

        try {
            List<WriteModel<Document>> updatesForOdkazovanV = new ArrayList<>();
            List<WriteModel<Document>> updatesForOdkazujeNa = new ArrayList<>();

            int docCount = 0;
            long totalDocs = collection1.countDocuments();
            System.out.println("Size: " + totalDocs);

            for (Document doc1 : collection1.find()) {
                Integer dokumentId = doc1.getInteger("znění-dokument-id");
                if (dokumentId == null) {
                    continue; // Skip if no dokumentId found
                }

                System.out.println("Processing doc: " + dokumentId);

                Set<Document> matchingIds1 = new HashSet<>();
                Set<Document> matchingIds2 = new HashSet<>();

                collection2.find(Filters.eq("znění-cíl-dokument-id", dokumentId))
                    .projection(Projections.include("znění-fragment-zdroj.znění-dokument-id"))
                    .forEach(doc2 -> {
                        Document fragmentZdroj = doc2.get("znění-fragment-zdroj", Document.class);
                        if (fragmentZdroj != null) {
                            Integer sourceId = fragmentZdroj.getInteger("znění-dokument-id");
                            if (sourceId != null) {
                                matchingIds1.add(new Document("znění-dokument-id", sourceId));
                            }
                        }
                    });

                collection2.find(Filters.eq("znění-fragment-zdroj.znění-dokument-id", dokumentId))
                    .projection(Projections.include("znění-cíl-dokument-id"))
                    .forEach(doc2 -> {
                        Integer targetId = doc2.getInteger("znění-cíl-dokument-id");
                        if (targetId != null) {
                            matchingIds2.add(new Document("znění-cíl-dokument-id", targetId));
                        }
                    });

                // Accumulate updates for bulkWrite
                updatesForOdkazovanV.add(
                    new UpdateOneModel<>(
                        Filters.eq("znění-dokument-id", dokumentId),
                        Updates.set("odkazován-v", matchingIds1)
                    )
                );

                updatesForOdkazujeNa.add(
                    new UpdateOneModel<>(
                        Filters.eq("znění-dokument-id", dokumentId),
                        Updates.set("odkazuje-na", matchingIds2)
                    )
                );

                docCount++;
                System.out.println("Processed document count: " + docCount + "/" + totalDocs);
            }

            // After processing all documents, perform the final bulk writes
            if (!updatesForOdkazovanV.isEmpty()) {
                collection1.bulkWrite(updatesForOdkazovanV);
            }
            if (!updatesForOdkazujeNa.isEmpty()) {
                collection1.bulkWrite(updatesForOdkazujeNa);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
     public void transferNewestDocuments(MongoCollection<Document> sourceCollection, MongoCollection<Document> targetCollection) {
        try {
            startTransfer(sourceCollection,targetCollection);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(PostProcessingService.class.getName()).log(Level.SEVERE, null, ex);
        }                        
    }

        private void processTerminDocument(Document docVazba,
                                       MongoCollection<Document> targetCollection,
                                       MongoCollection<Document> terminBaseCollection, MongoCollection<Document> terminDefiniceCollection
                                        ) {
        try {
            Integer terminID = getInteger(docVazba, "termín-id");
            Integer terminDefiniceID = getInteger(docVazba, "definice-termínu-id");
            logger.info("termín-id = " + terminID);
            logger.info("definice-termínu-id = " + terminDefiniceID);
           Document terminBase = mongoUtils.findInCollection(terminBaseCollection, "termín-id", terminID);
           Document terminDefinice = mongoUtils.findInCollection(terminDefiniceCollection, "definice-termínu-id", terminDefiniceID);
          
            
            Document newDoc = createVazbaDocument(terminID, terminDefiniceID,
                    terminBase.get("termín-název").toString(),terminDefinice.get("definice-termínu-text").toString(),
                    getZneniDokumentIdsById(terminDefinice,terminDefiniceID,"definice-termínu-vazba","právní-akt-znění-fragment"));
            
            
            // Insert the new document
            targetCollection.insertOne(newDoc);
            logger.info("Inserted new document with termin-id {}.", terminID);
        } catch (Exception e) {
            logger.error("Failed to process document: {}", e.getMessage());
        }
    }
        
            
   public List<Integer> getZneniDokumentIdsById(Document doc, Integer definiceTerminuId,String firstArrayName, String secondArrayName) {
        List<Integer> zneniDokumentIds = new ArrayList<>();
        try {
            Document vazbaList = doc.get("definice-termínu-vazba",Document.class);
            if (vazbaList == null || vazbaList.isEmpty()) {
                logger.warn("'definice-termínu-vazba' field is missing or empty for definice-termínu-id: {}", definiceTerminuId);
                return zneniDokumentIds; // Return empty list
            }
            

            List<Document> fragmentList = vazbaList.getList("právní-akt-znění-fragment",Document.class);
             if (fragmentList == null || fragmentList.isEmpty()) {
                logger.warn("'definice-termínu-vazba' array is missing or empty for definice-termínu-id: {}", definiceTerminuId);
                return zneniDokumentIds; // Return empty list
            }
            
            for (Document fragment : fragmentList) {
                Integer zneniDokumentId = fragment.getInteger("znění-dokument-id");
                if (zneniDokumentId != null) {
                    zneniDokumentIds.add(zneniDokumentId);
                    logger.debug("Extracted 'znění-dokument-id': {}", zneniDokumentId);
                } else {
                    logger.warn("'znění-dokument-id' is missing in a 'právní-akt-znění-fragment' object for definice-termínu-id: {}", definiceTerminuId);
                }
            }


        logger.info("Retrieved {} 'znění-dokument-id' values for definice-termínu-id: {}", zneniDokumentIds.size(), definiceTerminuId);

        } catch (Exception e) {
            logger.error("An error occurred while retrieving 'znění-dokument-id' values: ", e);
        }
        
        return zneniDokumentIds;
    }
   
    public List<Document> createArrayContainingPairs(String parameterName, List<Integer> zneniDokumentIds) {
        List<Document> dokumentPairs = zneniDokumentIds.stream()
                    .map(id -> new Document(parameterName, id))
                    .collect(Collectors.toList());
        return dokumentPairs;
    }
    
    
    private String fetchIdWithRetry(Integer zneniDokumentId, String format) {
        String cachedId = format.equals("PDF") ? PDF_ID_CACHE.get(zneniDokumentId) : DOCX_ID_CACHE.get(zneniDokumentId);
        if (cachedId != null) {
            return cachedId;
        }

        int attempt = 0;
        int delayMillis = INITIAL_DELAY_MILLIS;

        while (attempt < MAX_FETCH_ATTEMPTS) {
            attempt++;
            String id = fetchDocumentId(zneniDokumentId, format);
            if (id != null) {
                return id;
            }
            logger.info("Attempt {}/{}: Waiting {} ms before retrying for znění-dokument-id {} format {}.",
                    attempt, MAX_FETCH_ATTEMPTS, delayMillis, zneniDokumentId, format);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting to retry fetching ID: {}", e.getMessage());
                break;
            }
            delayMillis *= 1;
        }

        logger.warn("Exceeded max attempts for fetching ID for znění-dokument-id {} format {}.", zneniDokumentId, format);
        return null;
    }

    private String fetchDocumentId(Integer zneniDokumentId, String format) {
        String apiUrl = String.format("https://www.e-sbirka.cz/sbr-cache/stahni/informativni-zneni/%d/%s", zneniDokumentId, format);
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/json")
                .header("User-Agent", "curl/7.68.0")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
                JsonNode idNode = responseJson.get("id");
                if (idNode != null && !idNode.isNull()) {
                    return idNode.asText();
                } else {
                    logger.info("Document generation in progress for znění-dokument-id {} format {}.", zneniDokumentId, format);
                    return null;
                }
            } else {
                logger.warn("Failed to fetch document ID for znění-dokument-id {} format {}. Status Code: {}. Response: {}",
                        zneniDokumentId, format, response.code(), responseBody);
                return null;
            }
        } catch (IOException e) {
            logger.error("IOException while fetching document ID for znění-dokument-id {} format {}: {}",
                    zneniDokumentId, format, e.getMessage());
            return null;
        }
    }

    private Document createZneniDocument(Integer zneniDokumentId, Integer zneniBaseId, String aktNazevVyhlasen,
                                           String cisEsbTypZneniPoložka, String zneniDatumUcinnostiOdStr,
                                           String typSbirky,String podTyp, String oznaceníAktu,String aktCitace) {
        Document doc = new Document("znění-dokument-id", zneniDokumentId)
                .append("znění-base-id", zneniBaseId)
                .append("akt-název-vyhlášený", aktNazevVyhlasen)
                .append("akt-typ-sbírky", typSbirky)
                .append("akt-označení", oznaceníAktu)
                .append("akt-plné-označení",aktCitace)
                .append("typ-aktu", podTyp)
                .append("cis-esb-typ-znění-po", cisEsbTypZneniPoložka)
                .append("znění-datum-účinnosti-od", zneniDatumUcinnostiOdStr);
        return doc;
    }
   
     private Document createVazbaDocument(Integer terminID, Integer terminDefiniceID, String terminName, String definiceText,
                                           List<Integer> vztazeneDokumenty) {
        Document doc = new Document("termín-id", terminID)
                .append("definice_termínu-id", terminDefiniceID)
                .append("termin-nazev", terminName)
                .append("termin-definice", definiceText)
                .append("vztazene-dokumenty",createArrayContainingPairs("znění-dokument-id", vztazeneDokumenty));
               
           return doc;
    }

    private static Integer getInteger(Document doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.error("Invalid number format for {}: {}", key, value);
            }
        }
        return null;
    }

    private static Date parseDate(String dateString) {
        try {
            DATE_FORMAT.get().setLenient(false);
             return DATE_FORMAT.get().parse(dateString.trim());
       } catch (ParseException e) {
           logger.error("ParseException - Invalid date format: {}", dateString, e);
           return null;
       } catch (Exception e) {
           logger.error("Exception while parsing date: {}", dateString, e);
           return null;
       }
        
    }

    private static OkHttpClient createUnsafeOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string) throws CertificateException {
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .retryOnConnectionFailure(true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
