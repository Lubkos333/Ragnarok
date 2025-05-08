package cz.ragnarok.ragnarok.service;

import cz.ragnarok.ragnarok.rest.dto.ChromaDbResponseEntity;
import cz.ragnarok.ragnarok.rest.dto.ChunkDto;
import cz.ragnarok.ragnarok.rest.dto.DataByDesignationDto;
import org.springframework.ai.document.Document;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {

    @Autowired
    @Qualifier("dataClient")
    private WebClient dataClient;

    @Autowired
    private VectorDBService vectorDBService;

    public Mono<List<ChunkDto>> getParagraphsOnlyByDesignation(String designation) {
        return dataClient.get()
                .uri("/api/processing/getParagraphsOnlyByDesignation?designation="+ designation)
                .header("Authorization", "Bearer testApiKey")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChunkDto>>() {})
                .timeout(Duration.ofMinutes(5));
    }

    public Mono<DataByDesignationDto> getDataByDesignation(String designation) {
        return dataClient.get()
                .uri("/api/data/getByDesignation?designation="+ designation)
                .header("Authorization", "Bearer testApiKey")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<DataByDesignationDto>() {})
                .timeout(Duration.ofMinutes(5));
    }

    public List<Document> chunksToDocuments(List<ChunkDto> chunks, String designation, LocalDate date) {
        List<Document> docs = chunks.stream().map(
                chunkDto -> vectorDBService.makeDocument(chunkDto, designation, date)
        ).toList();

        return docs;
    }

    private void upload(List<ChunkDto> chunks, String designation, LocalDate date) {
        try {
            vectorDBService.uploadChunks(chunksToDocuments(chunks, designation, date));
        }
        catch (NonTransientAiException e) {
            List<ChunkDto> shortenChunks = new ArrayList<>();
            for (ChunkDto chunkDto : chunks) {
                shortenChunks.addAll(shortChunk(chunkDto));
            }
            upload(shortenChunks, designation, date);
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

    public String uploadDocumentByDesignation(String designation) {
        try {
            ChromaDbResponseEntity doc = vectorDBService.getByDesignation(designation, 1);
            LocalDate newDate = getDataByDesignation(designation).block().getZneniDatumUcinnostiOd();
            if(!doc.getIds().isEmpty()) {
                LocalDate oldDate = LocalDate.parse(doc.getMetadatas().getFirst().getDate());
                if (newDate.isAfter(oldDate)) {
                    vectorDBService.deleteByDesignation(designation);
                }
                else {
                    return "OK";
                }
            }
            saveShortenChunksByDesignation(designation, newDate);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "OK";
    }

    public void saveShortenChunksByDesignation(String designation, LocalDate newDate) {
        List<ChunkDto> chunks = getParagraphsOnlyByDesignation(designation).block();
        chunks = chunks.stream().toList();
        int chunksSize = chunks.size();
        int chunkSize = 10;
        for (int i = 0; i < chunksSize; i += chunkSize) {
            upload(chunks.subList(i, Math.min(i + chunkSize, chunksSize)), designation, newDate);
        }
    }

    public String deleteByDesignation(String designation) {
        try {
            vectorDBService.deleteByDesignation(designation);
            return "OK";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
