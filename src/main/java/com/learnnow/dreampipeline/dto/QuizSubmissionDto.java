package com.learnnow.dreampipeline.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * DTO for quiz submission request
 */
@Data
public class QuizSubmissionDto {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Topic ID is required")
    private Long topicId;
    
    @NotEmpty(message = "Answers array cannot be empty")
    private List<QuizAnswerDto> answers;
    
    private Integer timeTakenMinutes;
    
    @Data
    public static class QuizAnswerDto {
        @NotNull(message = "Quiz ID is required")
        private Long quizId;
        
        @NotNull(message = "Answer is required")
        private String answer;
    }
}