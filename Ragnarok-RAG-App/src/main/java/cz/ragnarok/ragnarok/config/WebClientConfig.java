package cz.ragnarok.ragnarok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "dataClient")
    public WebClient dataClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://document-api.dyn.cloud.e-infra.cz")
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024)
                )
                .build();
    }
}
