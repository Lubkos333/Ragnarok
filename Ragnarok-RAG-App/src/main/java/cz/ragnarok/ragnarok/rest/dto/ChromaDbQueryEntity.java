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
public class ChromaDbQueryEntity {
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> ids;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<List<Float>>> embeddings;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> documents;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> uris;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> data;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<Metadata>> metadatas;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> included;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<Float>> distances;

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
