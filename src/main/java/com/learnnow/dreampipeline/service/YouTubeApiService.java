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
 * Service for YouTube Data API v3 integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeApiService {
    
    @Qualifier("youtubeWebClient")
    private final WebClient youtubeWebClient;
    private final AppConfig appConfig;
    
    /**
     * Search for educational videos with captions for a given topic
     */
    public List<Map<String, Object>> searchVideosWithCaptions(String topicName) {
        try {
            log.info("Searching YouTube videos for topic: {}", topicName);
            
            // TODO: Replace with actual API integration when YouTube API key is available
            if ("YOUR_YOUTUBE_API_KEY_HERE".equals(appConfig.getYoutubeApiKey())) {
                log.warn("Using mock response - YouTube API key not configured");
                return generateMockVideoSearchResults(topicName);
            }
            
            // Search for videos
            String searchQuery = topicName + " tutorial educational";
            List<Map<String, Object>> searchResults = searchVideos(searchQuery);
            
            // Filter videos with captions
            return searchResults.stream()
                    .filter(video -> hasClosedCaptions((String) video.get("videoId")))
                    .limit(3) // Limit to top 3 videos per topic
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error searching YouTube videos for topic: {}", topicName, e);
            throw new ExternalApiException("YouTube", "Failed to search videos", e);
        }
    }
    
    /**
     * Search for videos using YouTube Data API
     */
    private List<Map<String, Object>> searchVideos(String query) {
        return youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("q", query)
                        .queryParam("type", "video")
                        .queryParam("order", "relevance")
                        .queryParam("maxResults", "10")
                        .queryParam("key", appConfig.getYoutubeApiKey())
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    log.error("YouTube search API error: {}", response.statusCode());
                    return Mono.error(new ExternalApiException("YouTube", response.statusCode().value(), "Video search failed"));
                })
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    return items.stream()
                            .map(this::mapVideoSearchResult)
                            .toList();
                })
                .block();
    }
    
    /**
     * Check if a video has closed captions available
     */
    public boolean hasClosedCaptions(String videoId) {
        try {
            if ("YOUR_YOUTUBE_API_KEY_HERE".equals(appConfig.getYoutubeApiKey())) {
                // Mock: assume all videos have captions for testing
                return true;
            }
            
            Map<String, Object> response = youtubeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/captions")
                            .queryParam("part", "snippet")
                            .queryParam("videoId", videoId)
                            .queryParam("key", appConfig.getYoutubeApiKey())
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), response_ -> {
                        log.warn("YouTube captions API error for video {}: {}", videoId, response_.statusCode());
                        return Mono.empty(); // Don't fail, just return false
                    })
                    .bodyToMono(Map.class)
                    .block();
            
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            boolean hasCaptions = items != null && !items.isEmpty();
            log.debug("Video {} has captions: {}", videoId, hasCaptions);
            return hasCaptions;
            
        } catch (Exception e) {
            log.warn("Error checking captions for video {}: {}", videoId, e.getMessage());
            return false; // Assume no captions if check fails
        }
    }
    
    /**
     * Get detailed video information including statistics
     */
    public Map<String, Object> getVideoDetails(String videoId) {
        try {
            if ("YOUR_YOUTUBE_API_KEY_HERE".equals(appConfig.getYoutubeApiKey())) {
                return generateMockVideoDetails(videoId);
            }
            
            return youtubeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,statistics,contentDetails")
                            .queryParam("id", videoId)
                            .queryParam("key", appConfig.getYoutubeApiKey())
                            .build())
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        log.error("YouTube video details API error: {}", response.statusCode());
                        return Mono.error(new ExternalApiException("YouTube", response.statusCode().value(), "Failed to get video details"));
                    })
                    .bodyToMono(Map.class)
                    .map(response -> {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                        if (items != null && !items.isEmpty()) {
                            return mapVideoDetails(items.get(0));
                        }
                        throw new ExternalApiException("YouTube", -1, "Video not found");
                    })
                    .block();
                    
        } catch (Exception e) {
            log.error("Error getting video details for: {}", videoId, e);
            throw new ExternalApiException("YouTube", "Failed to get video details", e);
        }
    }
    
    /**
     * Map YouTube search result to our format
     */
    private Map<String, Object> mapVideoSearchResult(Map<String, Object> item) {
        Map<String, Object> id = (Map<String, Object>) item.get("id");
        Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
        
        return Map.of(
            "videoId", id.get("videoId"),
            "title", snippet.get("title"),
            "description", snippet.get("description"),
            "channelTitle", snippet.get("channelTitle"),
            "publishedAt", snippet.get("publishedAt"),
            "url", "https://www.youtube.com/watch?v=" + id.get("videoId")
        );
    }
    
    /**
     * Map YouTube video details to our format
     */
    private Map<String, Object> mapVideoDetails(Map<String, Object> item) {
        Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
        Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
        Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
        
        return Map.of(
            "title", snippet.get("title"),
            "description", snippet.get("description"),
            "duration", contentDetails.get("duration"),
            "viewCount", statistics.get("viewCount"),
            "likeCount", statistics.get("likeCount"),
            "channelTitle", snippet.get("channelTitle")
        );
    }
    
    // Mock implementations for development/testing
    private List<Map<String, Object>> generateMockVideoSearchResults(String topicName) {
        return List.of(
            Map.of(
                "videoId", "mock_video_1_" + topicName.replaceAll(" ", "_"),
                "title", topicName + " - Complete Tutorial",
                "description", "Comprehensive tutorial covering " + topicName,
                "channelTitle", "Educational Channel",
                "publishedAt", "2024-01-01T00:00:00Z",
                "url", "https://www.youtube.com/watch?v=mock_video_1_" + topicName.replaceAll(" ", "_"),
                "viewCount", 125000L,
                "likeCount", 3500L,
                "durationMinutes", 25
            ),
            Map.of(
                "videoId", "mock_video_2_" + topicName.replaceAll(" ", "_"),
                "title", topicName + " Explained Simply", 
                "description", "Easy explanation of " + topicName,
                "channelTitle", "Tech Academy",
                "publishedAt", "2024-01-15T00:00:00Z",
                "url", "https://www.youtube.com/watch?v=mock_video_2_" + topicName.replaceAll(" ", "_"),
                "viewCount", 89000L,
                "likeCount", 2100L,
                "durationMinutes", 18
            )
        );
    }
    
    private Map<String, Object> generateMockVideoDetails(String videoId) {
        return Map.of(
            "title", "Mock Video Title",
            "description", "Mock video description for " + videoId,
            "duration", "PT15M30S",
            "viewCount", "50000",
            "likeCount", "1500",
            "channelTitle", "Mock Channel"
        );
    }
}