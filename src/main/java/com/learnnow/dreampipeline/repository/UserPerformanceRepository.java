package com.learnnow.dreampipeline.repository;

import com.learnnow.dreampipeline.entity.UserPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for UserPerformance entity operations
 */
@Repository
public interface UserPerformanceRepository extends JpaRepository<UserPerformance, Long> {
    
    List<UserPerformance> findByUserId(Long userId);
    
    List<UserPerformance> findByTopicId(Long topicId);
    
    List<UserPerformance> findByUserIdAndTopicId(Long userId, Long topicId);
    
    @Query("SELECT AVG(up.accuracy) FROM UserPerformance up WHERE up.user.id = :userId")
    Double getAverageAccuracyByUserId(@Param("userId") Long userId);
    
    @Query("SELECT AVG(up.learningRate) FROM UserPerformance up WHERE up.user.id = :userId")
    Double getAverageLearningRateByUserId(@Param("userId") Long userId);
    
    @Query("SELECT up FROM UserPerformance up WHERE up.user.id = :userId ORDER BY up.completedAt DESC")
    List<UserPerformance> findByUserIdOrderByCompletedAtDesc(@Param("userId") Long userId);
}