package cz.ragnarok.ragnarok.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OllamaChatConfig {

    @Bean
    private ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    private ChatClient client(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        return chatClientBuilder.defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory)).build();
    }
}
