package com.learnnow.dreampipeline.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for course response with full details
 */
@Data
public class CourseResponseDto {
    
    private Long courseId;
    private String courseName;
    private String courseDescription;
    private String summary;
    private LocalDateTime createdAt;
    private List<TopicDto> topics;
    
    @Data
    public static class TopicDto {
        private Long topicId;
        private String topicName;
        private String description;
        private List<VideoDto> videos;
        private List<QuizDto> quizzes;
    }
    
    @Data
    public static class VideoDto {
        private Long videoId;
        private String title;
        private String url;
        private String youtubeVideoId;
        private Boolean captionAvailable;
        private Integer durationMinutes;
        private Long viewCount;
        private Long likeCount;
    }
    
    @Data
    public static class QuizDto {
        private Long quizId;
        private String question;
        private List<String> options;
        private String correctAnswer;
        private String difficultyLevel;
    }
}