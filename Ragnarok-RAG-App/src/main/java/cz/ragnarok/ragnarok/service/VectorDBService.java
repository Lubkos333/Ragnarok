package cz.ragnarok.ragnarok.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.ragnarok.ragnarok.rest.dto.ChromaDbResponseEntity;
import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import cz.ragnarok.ragnarok.rest.dto.DeleteBodyDto;
import cz.ragnarok.ragnarok.rest.dto.GetBodyDto;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class VectorDBService {

    @Autowired
    @Qualifier("chromaDb")
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${vector-db.collection-id}")
    private String collectionId;

    @Value("${vector-db.address}")
    private String address;

    private final ObjectMapper mapper = new ObjectMapper();

    public String deleteByDesignation(String designation) throws Exception{
        String url = address+"/api/v1/collections/"+ collectionId +"/delete";
        Map<String, Object> where = new HashMap<>();
        where.put("designation", designation);
        DeleteBodyDto body = DeleteBodyDto.builder()
                .where(where)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), "UTF-8"));

            return EntityUtils.toString(client.execute(post).getEntity(), "UTF-8");
        }
    }

    public ChromaDbResponseEntity getByDesignation(String designation, Integer limit) throws Exception{
        String url = address+"/api/v1/collections/"+ collectionId +"/get";

        Map<String, Object> where = new HashMap<>();
        where.put("designation", designation);
        GetBodyDto body = GetBodyDto.builder()
                .where(where)
                .limit(limit)
                .build();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), "UTF-8"));

            return mapper.readValue(EntityUtils.toString(client.execute(post).getEntity(), "UTF-8"), ChromaDbResponseEntity.class);
        }
    }

    public Document makeDocument(ChunkDto chunk, String designation, LocalDate date) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("head", chunk.getHead());
        metadata.put("part", chunk.getPart());
        metadata.put("section", chunk.getSection());
        metadata.put("title", chunk.getTitle());
        metadata.put("paragraph", chunk.getParagraph());
        metadata.put("paragraphSubtitle", chunk.getParagraphSubtitle());
        metadata.put("content", chunk.getContent());
        metadata.put("designation",designation);
        metadata.put("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        Document doc = new Document(chunk.getContent(), metadata);
        doc.setEmbedding(embeddingModel.embed(doc));
        return doc;
    }

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withTopK(10)
                        .withQuery(query)
                        .withSimilarityThreshold(0.5)
        );
    }

    public void uploadChunks(List<Document> documents) {
        vectorStore.add(documents);
    }


}
