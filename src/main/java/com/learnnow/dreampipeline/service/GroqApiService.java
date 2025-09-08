package com.learnnow.dreampipeline.service;

import com.learnnow.dreampipeline.config.AppConfig;
import com.learnnow.dreampipeline.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Service for integrating with Groq API for AI-powered content generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroqApiService {
    
    @Qualifier("groqWebClient")
    private final WebClient groqWebClient;
    private final AppConfig appConfig;
    
    /**
     * Generate summary from course description using Groq API
     */
    public String generateCourseSummary(String courseDescription) {
        try {
            log.info("Generating course summary for description length: {}", courseDescription.length());
            
            // TODO: Replace with actual API integration when Groq API key is available
            if ("YOUR_GROQ_API_KEY_HERE".equals(appConfig.getGroqApiKey())) {
                log.warn("Using mock response - Groq API key not configured");
                return generateMockSummary(courseDescription);
            }
            
            Map<String, Object> requestBody = Map.of(
                "model", "mixtral-8x7b-32768",
                "messages", List.of(
                    Map.of("role", "system", "content", "You are an expert educational content summarizer. Create concise, informative summaries."),
                    Map.of("role", "user", "content", "Please provide a concise summary (100-150 words) of this course description: " + courseDescription)
                ),
                "max_tokens", 200,
                "temperature", 0.3
            );
            
            return groqWebClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + appConfig.getGroqApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        log.error("Groq API error: {}", response.statusCode());
                        return Mono.error(new ExternalApiException("Groq", response.statusCode().value(), "Failed to generate summary"));
                    })
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                            return (String) message.get("content");
                        }
                        throw new ExternalApiException("Groq", -1, "Invalid response format");
                    })
                    .block();
                    
        } catch (Exception e) {
            log.error("Error generating course summary", e);
            throw new ExternalApiException("Groq", "Failed to generate course summary", e);
        }
    }
    
    /**
     * Generate subtopics from course summary using Groq API
     */
    public List<String> generateSubtopics(String courseSummary) {
        try {
            log.info("Generating subtopics for summary length: {}", courseSummary.length());
            
            // TODO: Replace with actual API integration when Groq API key is available
            if ("YOUR_GROQ_API_KEY_HERE".equals(appConfig.getGroqApiKey())) {
                log.warn("Using mock response - Groq API key not configured");
                return generateMockSubtopics(courseSummary);
            }
            
            Map<String, Object> requestBody = Map.of(
                "model", "mixtral-8x7b-32768",
                "messages", List.of(
                    Map.of("role", "system", "content", "You are an expert curriculum designer. Break down course content into logical subtopics."),
                    Map.of("role", "user", "content", "Based on this course summary, generate 5-8 specific subtopics as a JSON array of strings: " + courseSummary)
                ),
                "max_tokens", 300,
                "temperature", 0.3
            );
            
            return groqWebClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + appConfig.getGroqApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        log.error("Groq API error: {}", response.statusCode());
                        return Mono.error(new ExternalApiException("Groq", response.statusCode().value(), "Failed to generate subtopics"));
                    })
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Parse response and extract subtopics
                        // This would need proper JSON parsing in real implementation
                        return generateMockSubtopics(courseSummary);
                    })
                    .block();
                    
        } catch (Exception e) {
            log.error("Error generating subtopics", e);
            throw new ExternalApiException("Groq", "Failed to generate subtopics", e);
        }
    }
    
    /**
     * Generate quiz questions from video transcript using Groq API
     */
    public List<Map<String, Object>> generateQuizQuestions(String transcript, String topicName) {
        try {
            log.info("Generating quiz questions for topic: {}", topicName);
            
            // TODO: Replace with actual API integration when Groq API key is available
            if ("YOUR_GROQ_API_KEY_HERE".equals(appConfig.getGroqApiKey())) {
                log.warn("Using mock response - Groq API key not configured");
                return generateMockQuizQuestions(topicName);
            }
            
            String prompt = String.format(
                "Based on this video transcript about '%s', generate %d multiple-choice questions. " +
                "Return as JSON array with objects containing: question, options (4 choices), correctAnswer. " +
                "Transcript: %s",
                topicName, appConfig.getDefaultQuestionsPerTopic(), transcript
            );
            
            Map<String, Object> requestBody = Map.of(
                "model", "mixtral-8x7b-32768",
                "messages", List.of(
                    Map.of("role", "system", "content", "You are an expert educational assessment creator. Generate clear, accurate multiple-choice questions."),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 800,
                "temperature", 0.3
            );
            
            return groqWebClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + appConfig.getGroqApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        log.error("Groq API error: {}", response.statusCode());
                        return Mono.error(new ExternalApiException("Groq", response.statusCode().value(), "Failed to generate quiz questions"));
                    })
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Parse response and extract quiz questions
                        // This would need proper JSON parsing in real implementation
                        return generateMockQuizQuestions(topicName);
                    })
                    .block();
                    
        } catch (Exception e) {
            log.error("Error generating quiz questions", e);
            throw new ExternalApiException("Groq", "Failed to generate quiz questions", e);
        }
    }
    
    // Mock implementations for development/testing
    private String generateMockSummary(String description) {
        return "This course provides comprehensive coverage of " + 
               description.substring(0, Math.min(description.length(), 50)) + 
               "... with practical examples and hands-on exercises designed to build real-world skills.";
    }
    
    private List<String> generateMockSubtopics(String summary) {
        return List.of(
            "Introduction and Fundamentals",
            "Core Concepts and Principles", 
            "Practical Implementation",
            "Advanced Techniques",
            "Best Practices and Patterns",
            "Real-world Applications",
            "Performance and Optimization"
        );
    }
    
    private List<Map<String, Object>> generateMockQuizQuestions(String topicName) {
        return List.of(
            Map.of(
                "question", "What is the primary concept covered in " + topicName + "?",
                "options", List.of("Option A", "Option B", "Option C", "Option D"),
                "correctAnswer", "Option B"
            ),
            Map.of(
                "question", "Which approach is recommended for " + topicName + "?",
                "options", List.of("Approach 1", "Approach 2", "Approach 3", "Approach 4"),
                "correctAnswer", "Approach 2"
            ),
            Map.of(
                "question", "What are the key benefits of " + topicName + "?",
                "options", List.of("Benefit A", "Benefit B", "Benefit C", "All of the above"),
                "correctAnswer", "All of the above"
            )
        );
    }
}