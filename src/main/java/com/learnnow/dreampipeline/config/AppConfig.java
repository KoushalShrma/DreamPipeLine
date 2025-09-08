package com.learnnow.dreampipeline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Configuration properties for application settings
 */
@Configuration
@Getter
public class AppConfig {
    
    @Value("${groq.api.key}")
    private String groqApiKey;
    
    @Value("${youtube.api.key}")
    private String youtubeApiKey;
    
    @Value("${app.quiz.default-questions-per-topic:7}")
    private Integer defaultQuestionsPerTopic;
    
    @Value("${app.quiz.difficulty-factor-base:1.0}")
    private Double difficultyFactorBase;
    
    @Value("${app.video.caption-required:true}")
    private Boolean captionRequired;
}