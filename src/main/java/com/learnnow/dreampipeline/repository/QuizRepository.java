package com.learnnow.dreampipeline.repository;

import com.learnnow.dreampipeline.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Quiz entity operations
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByTopicId(Long topicId);
    
    List<Quiz> findByTopicIdOrderByDifficultyLevel(Long topicId);
}