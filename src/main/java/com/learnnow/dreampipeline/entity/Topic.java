package com.learnnow.dreampipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Topic entity representing subtopics within a course
 */
@Entity
@Table(name = "topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Video> videos = new ArrayList<>();
    
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<Quiz> quizzes = new ArrayList<>();
    
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<UserPerformance> performances = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}