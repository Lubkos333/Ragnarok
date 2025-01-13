package cz.ragnarok.ragnarok.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BodyDto {
    private List<float[]> embeddings;
    private List<Map<String, Object>> metadata;
    private List<String> documents;
    private List<String> uris;
    private List<String> ids;

    public void addDocument (Document document) {
        embeddings.add(document.getEmbedding());
        metadata.add(document.getMetadata());
        documents.add(document.getContent());
        ids.add(document.getId());
        uris.add("");
    }
}
