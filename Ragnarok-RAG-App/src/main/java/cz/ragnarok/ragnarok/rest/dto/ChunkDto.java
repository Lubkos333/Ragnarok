package cz.ragnarok.ragnarok.rest.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChunkDto {
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String head = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String part = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String section = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String title = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String paragraph = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String paragraphSubtitle = "";
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String content = "";

    public ChunkDto(ChunkDto other) {
        this.head = other.head;
        this.part = other.part;
        this.section = other.section;
        this.title = other.title;
        this.paragraph = other.paragraph;
        this.paragraphSubtitle = other.paragraphSubtitle;
        this.content = other.content;
    }
}
