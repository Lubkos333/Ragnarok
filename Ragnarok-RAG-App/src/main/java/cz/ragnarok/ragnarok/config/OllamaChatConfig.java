package cz.ragnarok.ragnarok.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OllamaChatConfig {
    private final ChatClient chatClient;

    private OllamaChatConfig(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Bean
    public ChatClient chatClient() {
        return chatClient;
    }
}
