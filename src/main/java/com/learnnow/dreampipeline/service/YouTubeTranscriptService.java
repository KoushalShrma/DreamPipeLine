package com.learnnow.dreampipeline.service;

import com.learnnow.dreampipeline.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service for fetching YouTube video transcripts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeTranscriptService {
    
    @Qualifier("generalWebClient")
    private final WebClient generalWebClient;
    
    /**
     * Fetch transcript for a YouTube video
     * Note: This would typically use youtube-transcript-api or similar service
     */
    public String getVideoTranscript(String videoId) {
        try {
            log.info("Fetching transcript for video: {}", videoId);
            
            // TODO: Implement actual transcript fetching using youtube-transcript-api
            // This would typically involve calling a Python service or using a transcript API
            log.warn("Using mock transcript - Real transcript API not implemented");
            return generateMockTranscript(videoId);
            
        } catch (Exception e) {
            log.error("Error fetching transcript for video: {}", videoId, e);
            throw new ExternalApiException("YouTube Transcript", "Failed to fetch transcript", e);
        }
    }
    
    /**
     * Check if transcript is available for a video
     */
    public boolean isTranscriptAvailable(String videoId) {
        try {
            String transcript = getVideoTranscript(videoId);
            return transcript != null && !transcript.trim().isEmpty();
        } catch (Exception e) {
            log.warn("Transcript not available for video: {}", videoId);
            return false;
        }
    }
    
    /**
     * Clean and format transcript text
     */
    public String cleanTranscript(String rawTranscript) {
        if (rawTranscript == null || rawTranscript.trim().isEmpty()) {
            return "";
        }
        
        return rawTranscript
                // Remove timestamp markers like [00:15]
                .replaceAll("\\[\\d{2}:\\d{2}(:\\d{2})?\\]", "")
                // Remove speaker labels like "Speaker 1:"
                .replaceAll("Speaker \\d+:", "")
                // Clean up multiple whitespaces
                .replaceAll("\\s+", " ")
                // Remove common transcript artifacts
                .replaceAll("\\(music\\)", "")
                .replaceAll("\\(applause\\)", "")
                .replaceAll("\\(laughter\\)", "")
                .trim();
    }
    
    /**
     * Extract key topics from transcript (simple keyword extraction)
     */
    public String extractKeyTopics(String transcript) {
        if (transcript == null || transcript.trim().isEmpty()) {
            return "";
        }
        
        // Simple implementation - in production, you'd use NLP libraries
        String[] sentences = transcript.split("\\.");
        if (sentences.length > 0) {
            // Return first few sentences as key topics summary
            int maxSentences = Math.min(3, sentences.length);
            StringBuilder keyTopics = new StringBuilder();
            for (int i = 0; i < maxSentences; i++) {
                keyTopics.append(sentences[i].trim()).append(". ");
            }
            return keyTopics.toString().trim();
        }
        
        return transcript.substring(0, Math.min(200, transcript.length())) + "...";
    }
    
    // Mock implementation for development/testing
    private String generateMockTranscript(String videoId) {
        return String.format(
            "Welcome to this educational video about the topic. " +
            "In this tutorial, we will cover the fundamental concepts and principles. " +
            "First, let's start with the basic definitions and terminology. " +
            "Understanding these core concepts is essential for building a solid foundation. " +
            "Next, we'll explore practical examples and real-world applications. " +
            "These examples will help you understand how to apply the theoretical knowledge. " +
            "We'll also discuss common pitfalls and best practices to avoid mistakes. " +
            "Finally, we'll look at advanced techniques and optimization strategies. " +
            "By the end of this video, you should have a comprehensive understanding of the topic. " +
            "Thank you for watching, and don't forget to practice what you've learned. " +
            "[Video ID: %s]", videoId
        );
    }
}