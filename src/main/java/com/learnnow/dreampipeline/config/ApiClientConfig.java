package com.learnnow.dreampipeline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for external API clients
 */
@Configuration
public class ApiClientConfig {
    
    @Value("${groq.api.base-url}")
    private String groqBaseUrl;
    
    @Value("${youtube.api.base-url}")
    private String youtubeBaseUrl;
    
    /**
     * WebClient for Groq API calls
     */
    @Bean("groqWebClient")
    public WebClient groqWebClient() {
        return WebClient.builder()
                .baseUrl(groqBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    /**
     * WebClient for YouTube API calls
     */
    @Bean("youtubeWebClient")
    public WebClient youtubeWebClient() {
        return WebClient.builder()
                .baseUrl(youtubeBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    /**
     * General purpose WebClient for other external APIs
     */
    @Bean("generalWebClient")
    public WebClient generalWebClient() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}