package com.learnnow.dreampipeline.controller;

import com.learnnow.dreampipeline.dto.UserProgressDto;
import com.learnnow.dreampipeline.service.UserPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user progress and performance analytics
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserPerformanceService userPerformanceService;
    
    /**
     * Get comprehensive user progress and analytics
     * 
     * @param userId The user ID
     * @return Detailed user progress with course completion, performance metrics, and analytics
     */
    @GetMapping("/{userId}/progress")
    public ResponseEntity<UserProgressDto> getUserProgress(@PathVariable Long userId) {
        log.info("Retrieving progress for user ID: {}", userId);
        
        UserProgressDto progress = userPerformanceService.getUserProgress(userId);
        
        log.info("Generated progress report for user {} - Overall Accuracy: {:.1f}%, Topics: {}", 
                userId, progress.getOverallAccuracy(), progress.getTotalTopicsCompleted());
        
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Get user performance summary for dashboard
     * 
     * @param userId The user ID
     * @return Performance summary with key metrics
     */
    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getUserPerformanceSummary(@PathVariable Long userId) {
        log.info("Retrieving performance summary for user ID: {}", userId);
        
        Map<String, Object> summary = userPerformanceService.getUserPerformanceSummary(userId);
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get learning progress trend for a user (last 30 days)
     * 
     * @param userId The user ID
     * @return Daily learning trend data
     */
    @GetMapping("/{userId}/trend")
    public ResponseEntity<List<Map<String, Object>>> getLearningTrend(@PathVariable Long userId) {
        log.info("Retrieving learning trend for user ID: {}", userId);
        
        List<Map<String, Object>> trend = userPerformanceService.getLearningTrend(userId);
        
        return ResponseEntity.ok(trend);
    }
}