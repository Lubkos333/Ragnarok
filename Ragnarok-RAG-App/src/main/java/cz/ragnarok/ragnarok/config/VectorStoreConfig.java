package cz.ragnarok.ragnarok.config;

import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class VectorStoreConfig {

    @Bean
    public RestClient.Builder builder() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }


    @Bean
    public ChromaApi chromaApi(RestClient.Builder restClientBuilder) {
        String chromaUrl = "http://is-vector-db.dyn.cloud.e-infra.cz";
        ChromaApi chromaApi = new ChromaApi(chromaUrl, restClientBuilder);
        return chromaApi;
    }

    @Bean
    public VectorStore chromaVectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
        return new ChromaVectorStore(embeddingModel, chromaApi, "SpringAiCollection", false);
    }

}

