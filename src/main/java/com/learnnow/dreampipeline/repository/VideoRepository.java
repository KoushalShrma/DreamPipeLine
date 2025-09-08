package com.learnnow.dreampipeline.repository;

import com.learnnow.dreampipeline.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Video entity operations
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    List<Video> findByTopicId(Long topicId);
    
    Optional<Video> findByYoutubeVideoId(String youtubeVideoId);
    
    @Query("SELECT v FROM Video v WHERE v.topic.id = :topicId AND v.captionAvailable = true")
    List<Video> findByTopicIdWithCaptions(@Param("topicId") Long topicId);
    
    @Query("SELECT v FROM Video v WHERE v.captionAvailable = true ORDER BY v.viewCount DESC, v.likeCount DESC")
    List<Video> findAllWithCaptionsOrderByEngagement();
}