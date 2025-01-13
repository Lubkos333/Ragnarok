package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.BodyDto;
import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class VectorDBService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OllamaEmbeddingModel embeddingModel;

    @Autowired
    @Qualifier("vectorDBClient")
    private WebClient vectorDBClient;
    private final String collectionId = "9f8b5080-5f49-4050-b858-a1a0ccb8edaa";

    public Mono<String> upload(List<Document> documents) {
        return vectorDBClient.post()
                .uri("api/v1/collections/"+collectionId+"/add")
                .body(Mono.just(makeBody(documents)), BodyDto.class)
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMinutes(5));
    }


    public Document makeDocument(ChunkDto chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", chunk.getTitle());
        metadata.put("subTitle", chunk.getSubTitle());
        Document doc = new Document(chunk.getContent(), metadata);
        doc.setEmbedding(embeddingModel.embed(doc));
        return doc;
    }


    public BodyDto makeBody(List<Document> documents) {
        BodyDto bodyDto = new BodyDto(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for(Document doc : documents) {
            bodyDto.addDocument(doc);
        }
        return bodyDto;
    }

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(query);
    }


}
