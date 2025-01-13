package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class DataService {

    @Autowired
    @Qualifier("dataClient")
    private WebClient dataClient;

    @Autowired
    private VectorDBService vectorDBService;

    public Mono getChunks(String url) {
        return dataClient.get()
                .uri("/api/processing/getChunks?url="+url)
                .header("Authorization", "Bearer testApiKey")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChunkDto>>() {})
                .timeout(Duration.ofMinutes(5));
    }

    public List<Document> chunksToDocuments(Mono<List<ChunkDto>> chunks) {
        List<Document> docs = chunks.block().subList(0,10).stream().map(
                chunkDto -> vectorDBService.makeDocument(chunkDto)
        ).toList();

        return docs;
    }

    public void saveChunks() {
        List<Document> documents = chunksToDocuments(
                getChunks("https://www.e-sbirka.cz/souborove-sluzby/soubory/c69e52bb-26a3-4516-8088-85a3d88cafc1")
        );
        int chunkSize = 100;
        for (int i = 0; i < documents.size(); i += chunkSize){
            List<Document> chunk = documents.subList(i, Math.min(i + chunkSize, documents.size()));
            vectorDBService.upload(chunk);
        }
    }

}
