package cz.ragnarok.ragnarok.config;

import org.springframework.ai.autoconfigure.chat.client.ChatClientBuilderProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OllamaChatConfig {

    @Bean
    private ChatClient client(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }


    /*@Bean
    public OpenAiChatModel openAiChatModel(RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {
        OpenAiApi openAiApi = new OpenAiApi("https://chat.ai.e-infra.cz/",
                "sk-962b293a5578425784b8c4bd4ab79d09",
                "/api/chat/completions",
                "/api/embeddings",
                restClientBuilder,
                webClientBuilder,
                responseErrorHandler);
        return new OpenAiChatModel(openAiApi); // ✅ Nastavení nové endpoint path
    }*/
}
