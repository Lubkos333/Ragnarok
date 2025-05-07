package cz.ragnarok.ragnarok.rest.dto;

import cz.ragnarok.ragnarok.rest.enums.FlowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AnswerDto {
    private String answer;
    private String paragraphs;
    private FlowType flow;
}
