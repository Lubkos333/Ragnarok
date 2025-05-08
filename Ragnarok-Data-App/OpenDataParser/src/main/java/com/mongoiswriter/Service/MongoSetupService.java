/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mongoiswriter.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongoiswriter.Configuration.DataSourceConfig;
import com.mongoiswriter.Configuration.MongoConfig;
import com.mongoiswriter.Configuration.ProcessConfig;
import com.mongoiswriter.Enum.ExtracterType;
import com.mongoiswriter.Model.Segments;
import okhttp3.*;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;


@Service
@EnableAsync
public class MongoSetupService {
    private static final Logger logger = LoggerFactory.getLogger(MongoSetupService.class);
    
    private static final OkHttpClient HTTP_CLIENT = createUnsafeOkHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final MongoUtils mongoUtils;
    private final StringParser stringParser;
    private final SegmentsExtractionUtil segmentsExtractionUtil;
    private final MongoConfig mongoConfig;
    private final ProcessConfig processingConfig;
    private final DataSourceConfig dataSourceConfig;
    private final JsonExtracterUtil jsonExtracterUtil;
    
    public MongoSetupService(MongoUtils mongoUtils, StringParser stringParser, SegmentsExtractionUtil segmentsExtractionUtil, MongoConfig mongoConfig, ProcessConfig processingConfig,DataSourceConfig dataSourceConfig,JsonExtracterUtil jsonExtracterUtil) {
        this.mongoUtils = mongoUtils;
        this.stringParser = stringParser;
        this.segmentsExtractionUtil = segmentsExtractionUtil;
        this.mongoConfig = mongoConfig;
        this.processingConfig = processingConfig;
        this.dataSourceConfig = dataSourceConfig;
        this.jsonExtracterUtil = jsonExtracterUtil;
    }
    
    public void setupMongo() throws MalformedURLException, SocketException, IOException {
        
            mongoUtils.setProcessing(true);              
            try{
                
                jsonExtracterUtil.extractFromAddressToMongo(dataSourceConfig.URL_AKTY_ZNENI,mongoConfig.MONGO_COLLECTION_AKTY_ZNENI,ExtracterType.PRAVNI_AKT);
                mongoUtils.createCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL);
               
            
                MongoCollection<Document> zneniCollection = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_FINAL);
                MongoCollection<Document> zneniPravniAktCollection = mongoUtils.getMongoCollection(mongoConfig.MONGO_COLLECTION_AKTY_ZNENI);
                transferNewestDocuments(zneniPravniAktCollection,zneniCollection);
                
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
            System.out.println("Inserting document while transfering");
            targetCollection.insertOne(newDoc);
        }
    }
     
     public void transferNewestDocuments(MongoCollection<Document> sourceCollection, MongoCollection<Document> targetCollection) {
         System.out.println("Transfer neweest documents started");
        try {
            startTransfer(sourceCollection,targetCollection);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(MongoSetupService.class.getName()).log(Level.SEVERE, null, ex);
        }        
         System.out.println("Transfer neweest documents completed");
    }

          
   public List<Integer> getZneniDokumentIdsById(Document doc, Integer definiceTerminuId,String firstArrayName, String secondArrayName) {
        List<Integer> zneniDokumentIds = new ArrayList<>();
        try {
            
            Document vazbaList = doc.get("definice-termínu-vazba",Document.class);
            if (vazbaList == null || vazbaList.isEmpty()) {
                logger.warn("'definice-termínu-vazba' field is missing or empty for definice-termínu-id: {}", definiceTerminuId);
                return zneniDokumentIds; 
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