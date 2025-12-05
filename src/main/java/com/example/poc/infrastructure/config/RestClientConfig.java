package com.example.poc.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private final long readTimeoutSeconds;

    public RestClientConfig(
        @Value("${app.rest-client.read-timeout-seconds:10}") long readTimeoutSeconds
    ) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

        return RestClient.builder()
            .requestFactory(factory);
    }
}
