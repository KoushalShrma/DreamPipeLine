package com.learnnow.dreampipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserPerformance entity for tracking quiz results and learning analytics
 */
@Entity
@Table(name = "user_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPerformance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double accuracy; // Percentage (0-100)
    
    @Column(name = "learning_rate", nullable = false)
    private Double learningRate; // Calculated based on accuracy and difficulty
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "correct_answers")
    private Integer correctAnswers;
    
    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    
    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
}