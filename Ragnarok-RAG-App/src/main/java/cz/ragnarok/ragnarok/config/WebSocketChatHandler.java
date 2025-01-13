package cz.ragnarok.ragnarok.config;

import cz.ragnarok.ragnarok.service.VectorDBService;
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

    @Autowired
    private VectorDBService vectorDBService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String documents = getDocumentsString(message.getPayload());

        String prompt = buildRAGPrompt(message.getPayload(), documents);

        String response = ollamaUniqueQuestion(prompt);

        session.sendMessage(new TextMessage(response));
    }

    private String getDocumentsString(String query) {
        return String.join("\n",
                vectorDBService.search(query)
                        .stream()
                        .map(
                    doc -> doc.getContent()
                        ).toList()
        );
    }

    private String buildRAGPrompt(String question, String documents) {
        return "Odpověz uživateli na tuto otázku: \n" +
                question + "\n" +
                "Ale Odpověď můžeš čerpat jen z těchto informací: \n" +
                documents;
    }

    private String ollamaUniqueQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
