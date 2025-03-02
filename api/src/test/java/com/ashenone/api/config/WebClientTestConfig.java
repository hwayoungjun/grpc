package com.ashenone.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class WebClientTestConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
