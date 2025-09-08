package com.learnnow.dreampipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Quiz entity representing multiple-choice questions for topics
 */
@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;
    
    @Column(columnDefinition = "JSON", nullable = false)
    private String options; // JSON array of 4 options
    
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;
    
    @Column(name = "difficulty_level")
    private String difficultyLevel = "MEDIUM";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}