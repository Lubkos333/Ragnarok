package cz.ragnarok.ragnarok.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    @Autowired
    private ChatClient chatClient;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String response = chatClient.prompt()
                .user(message.getPayload())
                .call()
                .content();

        session.sendMessage(new TextMessage(response));
    }
}
