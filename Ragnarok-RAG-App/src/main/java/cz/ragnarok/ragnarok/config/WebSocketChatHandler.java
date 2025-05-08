package cz.ragnarok.ragnarok.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.ragnarok.ragnarok.rest.dto.AnswerDto;
import cz.ragnarok.ragnarok.rest.dto.MessageDto;
import cz.ragnarok.ragnarok.rest.enums.FlowType;
import cz.ragnarok.ragnarok.service.FlowService;
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
    private FlowService flowService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        try {

            MessageDto receivedMessage = objectMapper.readValue(message.getPayload(), MessageDto.class);

            if(receivedMessage.getNumberOfParagraphs() == null) {
                receivedMessage.setNumberOfParagraphs(15);
            }
            if(receivedMessage.getFlowType() == null) {
                receivedMessage.setFlowType(FlowType.CLASSIC);
            }

            AnswerDto response;


            switch (receivedMessage.getFlowType()) {
                case CLASSIC -> response = flowService.classicFlow(receivedMessage);
                case KEYWORDS -> response = flowService.keyWordsFlow(receivedMessage);
                case PARAPHRASE -> response = flowService.paraphraseFlow(receivedMessage, 0);
                default -> response = flowService.classicFlow(receivedMessage);
            }

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
        catch (Exception e) {
            session.sendMessage(new TextMessage("Omlouváme se, došlo k přerušení spojení. Zkuste prosím dotaz odeslat znovu nebo otevřete nové okno chatu."));
        }
    }

}
