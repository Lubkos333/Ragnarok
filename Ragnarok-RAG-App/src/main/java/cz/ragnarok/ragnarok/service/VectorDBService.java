package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.BodyDto;
import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import cz.ragnarok.ragnarok.rest.dto.FilterDto;
import org.springframework.ai.document.Document;
//import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;


@Service
public class VectorDBService {

    @Autowired
    private VectorStore vectorStore;

    /*@Autowired
    private OllamaEmbeddingModel embeddingModel;*/

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    @Qualifier("vectorDBClient")
    private WebClient vectorDBClient;

    @Value("${vector-db.collection-id}")
    private String collectionId;

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
        metadata.put("head", chunk.getHead());
        metadata.put("part", chunk.getPart());
        metadata.put("section", chunk.getSection());
        metadata.put("title", chunk.getTitle());
        metadata.put("paragraph", chunk.getParagraph());
        metadata.put("paragraphSubtitle", chunk.getParagraphSubtitle());
        metadata.put("content", chunk.getContent());
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
        return vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withTopK(10)
                        .withQuery(query)
                        .withSimilarityThreshold(0.5)
        );
    }

    public List<Document> filteredSearch(String query, FilterDto filter) {
        return vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withFilterExpression(
                                new FilterExpressionBuilder()
                                        .eq("head", "DĚDICKÉ PRÁVO")
                                        .build()
                        )
                        .withFilterExpression(
                                new FilterExpressionBuilder()
                                        .eq("paragraph", "§ 1660")
                                        .build()
                        )
                        .withTopK(3)
                        .withQuery(query)

                        .withSimilarityThreshold(0.5)
        );
    }

    public void makeFilter(FilterDto filter) throws NoSuchFieldException, IllegalAccessException {
        List<String> fieldNames = Arrays.stream(FilterDto.class.getDeclaredFields())
                .map(Field::getName)
                .toList();
        
        for (String fieldName: fieldNames) {
            if (fieldName != null) {
                Field field = FilterDto.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                String value = (String) field.get(filter);
            }
        }

        
        /*new FilterExpressionBuilder()
                .eq()*/
    }

    public void uploadChunks(List<Document> documents) {
        vectorStore.add(documents);
    }


}
