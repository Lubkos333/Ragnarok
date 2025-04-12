package cz.ragnarok.ragnarok.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FilterDto {
    private String head;
    private String part;
    private String section;
    private String title;
    private String paragraph;
    private String paragraphSubtitle;
    private String content;
}
