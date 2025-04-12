package cz.ragnarok.ragnarok.config;

import cz.ragnarok.ragnarok.service.FlowService;
import cz.ragnarok.ragnarok.service.VectorDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class WebSocketChatHandler extends TextWebSocketHandler {

    @Autowired
    private VectorDBService vectorDBService;

    @Autowired
    private FlowService flowService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String response = flowService.flow(message.getPayload());

        session.sendMessage(new TextMessage(response));
        //session.sendMessage(new TextMessage("RESPONSE"));
    }

}
