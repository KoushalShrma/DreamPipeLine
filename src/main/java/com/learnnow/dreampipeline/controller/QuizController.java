package com.learnnow.dreampipeline.controller;

import com.learnnow.dreampipeline.dto.QuizResultDto;
import com.learnnow.dreampipeline.dto.QuizSubmissionDto;
import com.learnnow.dreampipeline.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for quiz management and submission
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QuizController {
    
    private final QuizService quizService;
    
    /**
     * Submit quiz answers and get results with performance analytics
     * 
     * @param submission Quiz submission with user ID, topic ID, and answers
     * @return Quiz results with accuracy, learning rate, and feedback
     */
    @PostMapping("/submit")
    public ResponseEntity<QuizResultDto> submitQuiz(@Valid @RequestBody QuizSubmissionDto submission) {
        log.info("Processing quiz submission for user {} on topic {}", 
                submission.getUserId(), submission.getTopicId());
        
        QuizResultDto result = quizService.submitQuiz(submission);
        
        log.info("Quiz submission processed - Performance ID: {}, Accuracy: {:.1f}%", 
                result.getPerformanceId(), result.getAccuracy());
        
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
    
    /**
     * Get quiz results for a specific user and topic
     * 
     * @param userId The user ID
     * @param topicId The topic ID
     * @return List of quiz results for the user and topic
     */
    @GetMapping("/results")
    public ResponseEntity<List<QuizResultDto>> getQuizResults(
            @RequestParam Long userId, 
            @RequestParam Long topicId) {
        log.info("Retrieving quiz results for user {} and topic {}", userId, topicId);
        
        List<QuizResultDto> results = quizService.getQuizResults(userId, topicId);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get all quiz results for a user
     * 
     * @param userId The user ID
     * @return List of all quiz results for the user
     */
    @GetMapping("/results/user/{userId}")
    public ResponseEntity<List<QuizResultDto>> getUserQuizResults(@PathVariable Long userId) {
        log.info("Retrieving all quiz results for user {}", userId);
        
        List<QuizResultDto> results = quizService.getUserQuizResults(userId);
        
        return ResponseEntity.ok(results);
    }
}