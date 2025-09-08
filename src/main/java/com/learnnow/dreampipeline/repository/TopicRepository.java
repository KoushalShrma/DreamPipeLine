package com.learnnow.dreampipeline.repository;

import com.learnnow.dreampipeline.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Topic entity operations
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    List<Topic> findByCourseId(Long courseId);
    
    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.videos LEFT JOIN FETCH t.quizzes WHERE t.course.id = :courseId")
    List<Topic> findByCourseIdWithDetails(@Param("courseId") Long courseId);
}