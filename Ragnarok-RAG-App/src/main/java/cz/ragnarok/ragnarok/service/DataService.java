package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import org.springframework.ai.document.Document;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    @Autowired
    @Qualifier("dataClient")
    private WebClient dataClient;

    @Autowired
    private VectorDBService vectorDBService;

    public Mono<List<ChunkDto>> getChunks(String url) {
        return dataClient.get()
                .uri("/api/processing/getChunks?url="+url)
                .header("Authorization", "Bearer testApiKey")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChunkDto>>() {})
                .timeout(Duration.ofMinutes(5));
    }

    public Mono<List<ChunkDto>> getParagraphsByDesignation(String designation) {
        return dataClient.get()
                .uri("/api/processing/getParagraphsByDesignation?designation="+ designation)
                .header("Authorization", "Bearer testApiKey")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChunkDto>>() {})
                .timeout(Duration.ofMinutes(5));
    }
    public List<Document> chunksToDocuments(Mono<List<ChunkDto>> chunks) {
        List<Document> docs = chunks.block().stream().skip(30).map(
                chunkDto -> vectorDBService.makeDocument(chunkDto)
        ).toList();

        return docs;
    }

    public List<Document> chunksToDocuments(List<ChunkDto> chunks) {
        List<Document> docs = chunks.stream().map(
                chunkDto -> vectorDBService.makeDocument(chunkDto)
        ).toList();

        return docs;
    }

    public void saveChunks() {
        //List<ChunkDto> chunks = getChunks("https://www.e-sbirka.cz/souborove-sluzby/soubory/c69e52bb-26a3-4516-8088-85a3d88cafc1").block();
        List<ChunkDto> chunks = getParagraphsByDesignation("89/2012 Sb.").block();
        chunks = chunks.stream().toList();
        chunks = chunks.subList(0, chunks.size() - 1);
        int chunksSize = chunks.size();
        int chunkSize = 10;
        for (int i = 0; i < chunksSize; i += chunkSize){
            try {
                List<ChunkDto> chunk = chunks.subList(i, Math.min(i + chunkSize, chunksSize));
                vectorDBService.uploadChunks(chunksToDocuments(chunk));
            }
            catch (Exception e) {
                System.out.println(e);
                /*List<ChunkDto> chunk = chunks.subList(i, Math.min((i + chunkSize) -1 , chunksSize -1 ));
                vectorDBService.uploadChunks(chunksToDocuments(chunk));*/
            }
        }
    }

    public void saveShortenChunks() {
        //List<ChunkDto> chunks = getChunks("https://www.e-sbirka.cz/souborove-sluzby/soubory/c69e52bb-26a3-4516-8088-85a3d88cafc1").block();
        List<ChunkDto> chunks = getParagraphsByDesignation("89/2012 Sb.").block();
        chunks = chunks.stream().toList();
        int chunksSize = chunks.size();
        int chunkSize = 10;
        for (int i = 0; i < chunksSize; i += chunkSize){
            upload(chunks.subList(i, Math.min(i + chunkSize, chunksSize)));
        }
    }

    private void upload(List<ChunkDto> chunks) {
        try {
            vectorDBService.uploadChunks(chunksToDocuments(chunks));
        }
        catch (NonTransientAiException e) {
            List<ChunkDto> shortenChunks = new ArrayList<>();
            for (ChunkDto chunkDto : chunks) {
                shortenChunks.addAll(shortChunk(chunkDto));
            }
            upload(shortenChunks);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private List<ChunkDto> shortChunk(ChunkDto chunkDto) {
        int chunkLength = chunkDto.getContent().length();
        int contentLength = 400;
        if(chunkLength < contentLength) {
            return List.of(chunkDto);
        }
        else if(chunkLength > contentLength && chunkLength < contentLength * 2) {
            ChunkDto firstPart = new ChunkDto(chunkDto);
            ChunkDto secondPart = new ChunkDto(chunkDto);
            firstPart.setContent(firstPart.getContent().substring(0,contentLength));
            secondPart.setContent(secondPart.getContent().substring(Math.min(chunkLength - contentLength, contentLength)));
            return List.of(firstPart, secondPart);
        }
        else {
            return List.of();
        }
    }

}
