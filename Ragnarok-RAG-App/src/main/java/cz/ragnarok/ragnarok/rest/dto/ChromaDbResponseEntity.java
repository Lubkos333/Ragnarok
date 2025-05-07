package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChromaDbResponseEntity {
    private List<String> ids;
    private List<List<Float>> embeddings;
    private List<String> documents;
    private List<String> uris;
    private List<String> data;
    private List<Metadata> metadatas;
    private List<String> included;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Metadata {
        private String content;
        private String designation;
        private String head;
        private String paragraph;
        private String paragraphSubtitle;
        private String part;
        private String section;
        private String title;
        private String date;
    }
}
