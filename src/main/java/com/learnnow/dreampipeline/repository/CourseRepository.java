package com.learnnow.dreampipeline.repository;

import com.learnnow.dreampipeline.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Course entity operations
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByUserId(Long userId);
    
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.topics t LEFT JOIN FETCH t.videos LEFT JOIN FETCH t.quizzes WHERE c.id = :courseId")
    Optional<Course> findByIdWithTopicsAndDetails(@Param("courseId") Long courseId);
    
    @Query("SELECT c FROM Course c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Course> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}