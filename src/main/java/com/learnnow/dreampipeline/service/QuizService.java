package com.learnnow.dreampipeline.service;

import com.learnnow.dreampipeline.config.AppConfig;
import com.learnnow.dreampipeline.dto.QuizResultDto;
import com.learnnow.dreampipeline.dto.QuizSubmissionDto;
import com.learnnow.dreampipeline.entity.*;
import com.learnnow.dreampipeline.exception.ResourceNotFoundException;
import com.learnnow.dreampipeline.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for quiz management and performance tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserPerformanceRepository performanceRepository;
    private final VideoRepository videoRepository;
    
    private final AppConfig appConfig;
    
    /**
     * Submit quiz answers and calculate performance metrics
     */
    public QuizResultDto submitQuiz(QuizSubmissionDto submission) {
        log.info("Processing quiz submission for user {} on topic {}", 
                submission.getUserId(), submission.getTopicId());
        
        // Validate user and topic exist
        User user = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", submission.getUserId()));
        
        Topic topic = topicRepository.findById(submission.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", submission.getTopicId()));
        
        // Get all quizzes for the topic to validate submission
        List<Quiz> topicQuizzes = quizRepository.findByTopicId(submission.getTopicId());
        
        if (topicQuizzes.isEmpty()) {
            throw new ResourceNotFoundException("No quizzes found for topic: " + submission.getTopicId());
        }
        
        // Create a map of quiz ID to correct answer for quick lookup
        Map<Long, String> correctAnswers = topicQuizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, Quiz::getCorrectAnswer));
        
        // Calculate quiz results
        int totalQuestions = submission.getAnswers().size();
        int correctCount = 0;
        
        for (QuizSubmissionDto.QuizAnswerDto answer : submission.getAnswers()) {
            String correctAnswer = correctAnswers.get(answer.getQuizId());
            if (correctAnswer != null && correctAnswer.equals(answer.getAnswer())) {
                correctCount++;
            }
        }
        
        // Calculate accuracy percentage
        double accuracy = (double) correctCount / totalQuestions * 100.0;
        
        // Calculate learning rate based on accuracy and difficulty factors
        double learningRate = calculateLearningRate(accuracy, topic);
        
        // Save performance record
        UserPerformance performance = new UserPerformance();
        performance.setUser(user);
        performance.setTopic(topic);
        performance.setAccuracy(accuracy);
        performance.setLearningRate(learningRate);
        performance.setTotalQuestions(totalQuestions);
        performance.setCorrectAnswers(correctCount);
        performance.setTimeTakenMinutes(submission.getTimeTakenMinutes());
        
        UserPerformance savedPerformance = performanceRepository.save(performance);
        
        log.info("Quiz completed - User: {}, Topic: {}, Accuracy: {:.1f}%, Learning Rate: {:.3f}",
                user.getId(), topic.getName(), accuracy, learningRate);
        
        return mapToResultDto(savedPerformance);
    }
    
    /**
     * Get quiz results for a specific user and topic
     */
    @Transactional(readOnly = true)
    public List<QuizResultDto> getQuizResults(Long userId, Long topicId) {
        List<UserPerformance> performances = performanceRepository.findByUserIdAndTopicId(userId, topicId);
        
        return performances.stream()
                .map(this::mapToResultDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all quiz results for a user
     */
    @Transactional(readOnly = true)
    public List<QuizResultDto> getUserQuizResults(Long userId) {
        List<UserPerformance> performances = performanceRepository.findByUserIdOrderByCompletedAtDesc(userId);
        
        return performances.stream()
                .map(this::mapToResultDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate learning rate based on accuracy and difficulty factors
     * Formula: learningRate = (accuracy / 100) * difficultyFactor
     * difficultyFactor = base + (videoLengthMinutes / 60)
     */
    private double calculateLearningRate(double accuracy, Topic topic) {
        // Get average video duration for the topic
        List<Video> topicVideos = videoRepository.findByTopicId(topic.getId());
        
        double averageVideoLength = topicVideos.stream()
                .filter(video -> video.getDurationMinutes() != null)
                .mapToInt(Video::getDurationMinutes)
                .average()
                .orElse(20.0); // Default to 20 minutes if no duration data
        
        // Calculate difficulty factor
        double difficultyFactor = appConfig.getDifficultyFactorBase() + (averageVideoLength / 60.0);
        
        // Calculate learning rate
        double learningRate = (accuracy / 100.0) * difficultyFactor;
        
        log.debug("Learning rate calculation - Accuracy: {:.1f}%, Avg Video Length: {:.1f}min, " +
                "Difficulty Factor: {:.3f}, Learning Rate: {:.3f}",
                accuracy, averageVideoLength, difficultyFactor, learningRate);
        
        return Math.round(learningRate * 1000.0) / 1000.0; // Round to 3 decimal places
    }
    
    /**
     * Generate performance feedback based on quiz results
     */
    private String generateFeedback(UserPerformance performance) {
        double accuracy = performance.getAccuracy();
        
        if (accuracy >= 90) {
            return "Excellent work! You have a strong understanding of " + performance.getTopic().getName();
        } else if (accuracy >= 80) {
            return "Good job! You're doing well with " + performance.getTopic().getName() + 
                   ". Consider reviewing a few concepts to reach mastery.";
        } else if (accuracy >= 70) {
            return "You're making progress on " + performance.getTopic().getName() + 
                   ". Review the video content and try the quiz again to improve your understanding.";
        } else if (accuracy >= 60) {
            return "You're on the right track with " + performance.getTopic().getName() + 
                   ". Spend more time with the learning materials and practice more.";
        } else {
            return "This topic needs more attention. Review the videos for " + 
                   performance.getTopic().getName() + " and consider additional resources.";
        }
    }
    
    /**
     * Map UserPerformance entity to QuizResultDto
     */
    private QuizResultDto mapToResultDto(UserPerformance performance) {
        QuizResultDto dto = new QuizResultDto();
        dto.setPerformanceId(performance.getId());
        dto.setUserId(performance.getUser().getId());
        dto.setTopicId(performance.getTopic().getId());
        dto.setTopicName(performance.getTopic().getName());
        dto.setAccuracy(performance.getAccuracy());
        dto.setLearningRate(performance.getLearningRate());
        dto.setTotalQuestions(performance.getTotalQuestions());
        dto.setCorrectAnswers(performance.getCorrectAnswers());
        dto.setTimeTakenMinutes(performance.getTimeTakenMinutes());
        dto.setCompletedAt(performance.getCompletedAt());
        dto.setFeedback(generateFeedback(performance));
        
        return dto;
    }
}