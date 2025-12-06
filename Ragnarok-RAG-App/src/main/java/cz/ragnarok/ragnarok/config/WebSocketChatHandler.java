package cz.ragnarok.ragnarok.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import cz.ragnarok.ragnarok.service.flow.ClassicFlow;
import cz.ragnarok.ragnarok.service.flow.FlowInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
@Slf4j
public class WebSocketChatHandler extends TextWebSocketHandler {


    private final ObjectMapper objectMapper;

    private final List<FlowInterface> flows;

    private final ClassicFlow defaultFlow;

    public WebSocketChatHandler(List<FlowInterface> flows, ClassicFlow defaultFlow) {
        this.objectMapper = new ObjectMapper();
        this.flows = flows;
        this.defaultFlow = defaultFlow;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("✅ WebSocket connected: {}", session.getId());
        session.sendMessage(new TextMessage("Connection established ✅"));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("📩 Message received: {}", message.getPayload());

        try {

            MessageDto receivedMessage = objectMapper.readValue(message.getPayload(), MessageDto.class);

            if (receivedMessage.getNumberOfParagraphs() == null) {
                receivedMessage.setNumberOfParagraphs(15);
            }
            if (receivedMessage.getFlowType() == null) {
                receivedMessage.setFlowType(FlowType.CLASSIC);
            }

            AnswerDto response;

            FlowInterface flow = flows.stream()
                    .filter(f -> f.getType().equals(receivedMessage.getFlowType()))
                    .findFirst()
                    .orElse(defaultFlow);

            response = flow.flow(receivedMessage, session);


            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            session.sendMessage(new TextMessage("Omlouváme se, došlo k přerušení spojení. Zkuste prosím dotaz odeslat znovu nebo otevřete nové okno chatu."));
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("❌ WebSocket disconnected: {}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("⚠️ WebSocket error: {}", exception.getMessage(), exception);
    }

}
