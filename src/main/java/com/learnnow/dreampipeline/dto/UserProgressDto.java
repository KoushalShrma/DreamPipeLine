package com.learnnow.dreampipeline.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for user progress and performance analytics
 */
@Data
public class UserProgressDto {
    
    private Long userId;
    private String username;
    private Double overallAccuracy;
    private Double averageLearningRate;
    private Integer totalCoursesCompleted;
    private Integer totalTopicsCompleted;
    private List<CourseProgressDto> courseProgress;
    private List<RecentPerformanceDto> recentPerformances;
    
    @Data
    public static class CourseProgressDto {
        private Long courseId;
        private String courseName;
        private Integer totalTopics;
        private Integer completedTopics;
        private Double progressPercentage;
        private Double averageAccuracy;
        private LocalDateTime lastActivity;
    }
    
    @Data
    public static class RecentPerformanceDto {
        private Long topicId;
        private String topicName;
        private String courseName;
        private Double accuracy;
        private Double learningRate;
        private LocalDateTime completedAt;
    }
}