package cz.ragnarok.ragnarok.config;

import org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingProperties;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

//@Configuration
public class OpenAiConfig {

    /*@Bean
    @Primary
    public OpenAiEmbeddingProperties openAiEmbeddingClient() {
        OpenAiEmbeddingProperties props = new OpenAiEmbeddingProperties();
        props.setEmbeddingsPath("/api/embeddings");
        props.setMetadataMode(MetadataMode.EMBED);
        props.setOptions(
                OpenAiEmbeddingOptions.builder().withModel("nomic-embed-text").build()
        );
        return props;
    }*/

    /*@Bean
    @Primary
    public OpenAiApi openAiApi(String baseUrl, String apiKey, String completionsPath, String embeddingsPath, RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {
        return new OpenAiApi(baseUrl, apiKey, CollectionUtils.toMultiValueMap(Map.of()), completionsPath, embeddingsPath, restClientBuilder, webClientBuilder, responseErrorHandler);
    }*/
}
