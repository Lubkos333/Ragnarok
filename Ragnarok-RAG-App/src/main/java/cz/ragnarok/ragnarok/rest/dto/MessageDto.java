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
public class MessageDto {
    private String question;
    private String conversationId;
    private FlowType flowType;
}
