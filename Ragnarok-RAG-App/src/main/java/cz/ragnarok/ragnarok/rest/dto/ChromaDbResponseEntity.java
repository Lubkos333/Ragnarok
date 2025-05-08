package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChromaDbResponseEntity {
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> ids;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<Float>> embeddings;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> documents;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> uris;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> data;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<Metadata> metadatas;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
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
