package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChromaDbQueryEntity {
    private List<List<String>> ids;
    private List<List<List<Float>>> embeddings;
    private List<List<String>> documents;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> uris;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<List<String>> data;
    private List<List<Metadata>> metadatas;
    private List<String> included;
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
