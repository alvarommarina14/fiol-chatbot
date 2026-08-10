package com.fiol.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient whatsAppRestClient(RestClient.Builder builder, WhatsAppProperties properties) {
        // Must be the auto-configured builder: the static RestClient.builder() creates its
        // own converters with a default JsonMapper, which would ignore the SNAKE_CASE naming
        // strategy and send messagingProduct instead of the messaging_product Meta requires.
        return builder
                .baseUrl(properties.graphBaseUrl() + "/" + properties.apiVersion())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
