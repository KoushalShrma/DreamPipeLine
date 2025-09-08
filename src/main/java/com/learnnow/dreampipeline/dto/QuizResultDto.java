package com.learnnow.dreampipeline.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for quiz submission response
 */
@Data
public class QuizResultDto {
    
    private Long performanceId;
    private Long userId;
    private Long topicId;
    private String topicName;
    private Double accuracy;
    private Double learningRate;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenMinutes;
    private LocalDateTime completedAt;
    private String feedback;
}