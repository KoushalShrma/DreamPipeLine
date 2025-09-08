package com.learnnow.dreampipeline.service;

import com.learnnow.dreampipeline.dto.UserProgressDto;
import com.learnnow.dreampipeline.entity.Course;
import com.learnnow.dreampipeline.entity.User;
import com.learnnow.dreampipeline.entity.UserPerformance;
import com.learnnow.dreampipeline.exception.ResourceNotFoundException;
import com.learnnow.dreampipeline.repository.CourseRepository;
import com.learnnow.dreampipeline.repository.UserPerformanceRepository;
import com.learnnow.dreampipeline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for user performance analytics and progress tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserPerformanceService {
    
    private final UserRepository userRepository;
    private final UserPerformanceRepository performanceRepository;
    private final CourseRepository courseRepository;
    
    /**
     * Get comprehensive user progress and analytics
     */
    public UserProgressDto getUserProgress(Long userId) {
        log.info("Generating progress report for user: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Get user's performance data
        List<UserPerformance> performances = performanceRepository.findByUserIdOrderByCompletedAtDesc(userId);
        
        // Get user's courses
        List<Course> userCourses = courseRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Calculate overall metrics
        Double overallAccuracy = performanceRepository.getAverageAccuracyByUserId(userId);
        Double averageLearningRate = performanceRepository.getAverageLearningRateByUserId(userId);
        
        UserProgressDto progressDto = new UserProgressDto();
        progressDto.setUserId(userId);
        progressDto.setUsername(user.getUsername());
        progressDto.setOverallAccuracy(overallAccuracy != null ? overallAccuracy : 0.0);
        progressDto.setAverageLearningRate(averageLearningRate != null ? averageLearningRate : 0.0);
        progressDto.setTotalCoursesCompleted(userCourses.size());
        
        // Count unique topics completed (topics with at least one quiz attempt)
        int totalTopicsCompleted = (int) performances.stream()
                .map(p -> p.getTopic().getId())
                .distinct()
                .count();
        progressDto.setTotalTopicsCompleted(totalTopicsCompleted);
        
        // Generate course progress details
        List<UserProgressDto.CourseProgressDto> courseProgress = generateCourseProgress(userCourses, performances);
        progressDto.setCourseProgress(courseProgress);
        
        // Get recent performances (last 10)
        List<UserProgressDto.RecentPerformanceDto> recentPerformances = performances.stream()
                .limit(10)
                .map(this::mapToRecentPerformanceDto)
                .collect(Collectors.toList());
        progressDto.setRecentPerformances(recentPerformances);
        
        log.info("Generated progress report for user {} - Overall Accuracy: {:.1f}%, Topics Completed: {}",
                userId, overallAccuracy != null ? overallAccuracy : 0.0, totalTopicsCompleted);
        
        return progressDto;
    }
    
    /**
     * Get user performance summary for dashboard
     */
    public Map<String, Object> getUserPerformanceSummary(Long userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<UserPerformance> performances = performanceRepository.findByUserIdOrderByCompletedAtDesc(userId);
        
        if (performances.isEmpty()) {
            return Map.of(
                "totalQuizzes", 0,
                "averageAccuracy", 0.0,
                "averageLearningRate", 0.0,
                "topicsCompleted", 0,
                "lastActivity", null
            );
        }
        
        double averageAccuracy = performances.stream()
                .mapToDouble(UserPerformance::getAccuracy)
                .average()
                .orElse(0.0);
        
        double averageLearningRate = performances.stream()
                .mapToDouble(UserPerformance::getLearningRate)
                .average()
                .orElse(0.0);
        
        int uniqueTopics = (int) performances.stream()
                .map(p -> p.getTopic().getId())
                .distinct()
                .count();
        
        LocalDateTime lastActivity = performances.get(0).getCompletedAt();
        
        return Map.of(
            "totalQuizzes", performances.size(),
            "averageAccuracy", Math.round(averageAccuracy * 10.0) / 10.0,
            "averageLearningRate", Math.round(averageLearningRate * 1000.0) / 1000.0,
            "topicsCompleted", uniqueTopics,
            "lastActivity", lastActivity
        );
    }
    
    /**
     * Get learning progress trend for a user (last 30 days)
     */
    public List<Map<String, Object>> getLearningTrend(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<UserPerformance> performances = performanceRepository.findByUserIdOrderByCompletedAtDesc(userId);
        
        // Group by date and calculate daily averages
        Map<String, List<UserPerformance>> performancesByDate = performances.stream()
                .filter(p -> p.getCompletedAt().isAfter(LocalDateTime.now().minusDays(30)))
                .collect(Collectors.groupingBy(p -> p.getCompletedAt().toLocalDate().toString()));
        
        return performancesByDate.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<UserPerformance> dayPerformances = entry.getValue();
                    
                    double avgAccuracy = dayPerformances.stream()
                            .mapToDouble(UserPerformance::getAccuracy)
                            .average()
                            .orElse(0.0);
                    
                    double avgLearningRate = dayPerformances.stream()
                            .mapToDouble(UserPerformance::getLearningRate)
                            .average()
                            .orElse(0.0);
                    
                    return Map.<String, Object>of(
                        "date", date,
                        "averageAccuracy", Math.round(avgAccuracy * 10.0) / 10.0,
                        "averageLearningRate", Math.round(avgLearningRate * 1000.0) / 1000.0,
                        "quizzesCompleted", dayPerformances.size()
                    );
                })
                .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
                .collect(Collectors.toList());
    }
    
    /**
     * Generate course progress details
     */
    private List<UserProgressDto.CourseProgressDto> generateCourseProgress(
            List<Course> courses, List<UserPerformance> performances) {
        
        // Group performances by course
        Map<Long, List<UserPerformance>> performancesByCourse = performances.stream()
                .collect(Collectors.groupingBy(p -> p.getTopic().getCourse().getId()));
        
        return courses.stream()
                .map(course -> {
                    UserProgressDto.CourseProgressDto courseProgressDto = new UserProgressDto.CourseProgressDto();
                    courseProgressDto.setCourseId(course.getId());
                    courseProgressDto.setCourseName(course.getName());
                    
                    int totalTopics = course.getTopics().size();
                    courseProgressDto.setTotalTopics(totalTopics);
                    
                    List<UserPerformance> coursePerformances = performancesByCourse.getOrDefault(course.getId(), List.of());
                    
                    int completedTopics = (int) coursePerformances.stream()
                            .map(p -> p.getTopic().getId())
                            .distinct()
                            .count();
                    courseProgressDto.setCompletedTopics(completedTopics);
                    
                    double progressPercentage = totalTopics > 0 ? (double) completedTopics / totalTopics * 100.0 : 0.0;
                    courseProgressDto.setProgressPercentage(Math.round(progressPercentage * 10.0) / 10.0);
                    
                    double averageAccuracy = coursePerformances.stream()
                            .mapToDouble(UserPerformance::getAccuracy)
                            .average()
                            .orElse(0.0);
                    courseProgressDto.setAverageAccuracy(Math.round(averageAccuracy * 10.0) / 10.0);
                    
                    LocalDateTime lastActivity = coursePerformances.stream()
                            .map(UserPerformance::getCompletedAt)
                            .max(LocalDateTime::compareTo)
                            .orElse(course.getCreatedAt());
                    courseProgressDto.setLastActivity(lastActivity);
                    
                    return courseProgressDto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Map UserPerformance to RecentPerformanceDto
     */
    private UserProgressDto.RecentPerformanceDto mapToRecentPerformanceDto(UserPerformance performance) {
        UserProgressDto.RecentPerformanceDto dto = new UserProgressDto.RecentPerformanceDto();
        dto.setTopicId(performance.getTopic().getId());
        dto.setTopicName(performance.getTopic().getName());
        dto.setCourseName(performance.getTopic().getCourse().getName());
        dto.setAccuracy(performance.getAccuracy());
        dto.setLearningRate(performance.getLearningRate());
        dto.setCompletedAt(performance.getCompletedAt());
        return dto;
    }
}