package cz.ragnarok.ragnarok.service.flow;

import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import org.springframework.web.socket.WebSocketSession;

public interface FlowInterface {
    AnswerDto flow(MessageDto messageDto, WebSocketSession session);
    FlowType getType();
}
