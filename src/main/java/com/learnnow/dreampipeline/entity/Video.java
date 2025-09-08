package com.learnnow.dreampipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Video entity representing YouTube videos linked to topics
 */
@Entity
@Table(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "youtube_video_id", nullable = false)
    private String youtubeVideoId;
    
    @Column(name = "caption_available", nullable = false)
    private Boolean captionAvailable = false;
    
    @Column(columnDefinition = "TEXT")
    private String transcript;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "view_count")
    private Long viewCount;
    
    @Column(name = "like_count")
    private Long likeCount;
    
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