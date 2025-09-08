package com.learnnow.dreampipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnnow.dreampipeline.dto.CourseRequestDto;
import com.learnnow.dreampipeline.dto.CourseResponseDto;
import com.learnnow.dreampipeline.entity.*;
import com.learnnow.dreampipeline.exception.ResourceNotFoundException;
import com.learnnow.dreampipeline.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing courses and orchestrating the learning pipeline
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final VideoRepository videoRepository;
    private final QuizRepository quizRepository;
    
    private final GroqApiService groqApiService;
    private final YouTubeApiService youTubeApiService;
    private final YouTubeTranscriptService transcriptService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Create a new course and trigger the complete learning pipeline
     */
    public CourseResponseDto createCourse(CourseRequestDto request) {
        log.info("Creating course: {}", request.getCourseName());
        
        // Get or create user (for demo, create a default user if not specified)
        User user = getOrCreateUser(request.getUserId());
        
        // 1. Create and save course
        Course course = new Course();
        course.setName(request.getCourseName());
        course.setDescription(request.getCourseDescription());
        course.setUser(user);
        
        // 2. Generate summary using Groq API
        String summary = groqApiService.generateCourseSummary(request.getCourseDescription());
        course.setSummary(summary);
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created with ID: {}", savedCourse.getId());
        
        // 3. Generate subtopics from summary
        List<String> subtopics = groqApiService.generateSubtopics(summary);
        
        // 4. Process each subtopic
        List<Topic> topics = new ArrayList<>();
        for (String subtopicName : subtopics) {
            Topic topic = processSubtopic(savedCourse, subtopicName);
            topics.add(topic);
        }
        
        savedCourse.setTopics(topics);
        log.info("Course pipeline completed for: {}", savedCourse.getName());
        
        return mapToResponseDto(savedCourse);
    }
    
    /**
     * Get course with full details
     */
    @Transactional(readOnly = true)
    public CourseResponseDto getCourseById(Long courseId) {
        Course course = courseRepository.findByIdWithTopicsAndDetails(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        
        return mapToResponseDto(course);
    }
    
    /**
     * Get all courses for a user
     */
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getCoursesByUserId(Long userId) {
        List<Course> courses = courseRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return courses.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Process a single subtopic: find videos, get transcripts, generate quizzes
     */
    private Topic processSubtopic(Course course, String subtopicName) {
        log.info("Processing subtopic: {}", subtopicName);
        
        // Create topic
        Topic topic = new Topic();
        topic.setName(subtopicName);
        topic.setDescription("Learn about " + subtopicName);
        topic.setCourse(course);
        
        Topic savedTopic = topicRepository.save(topic);
        
        // Find videos with captions
        List<Map<String, Object>> videoResults = youTubeApiService.searchVideosWithCaptions(subtopicName);
        
        List<Video> videos = new ArrayList<>();
        for (Map<String, Object> videoData : videoResults) {
            Video video = processVideo(savedTopic, videoData);
            if (video != null) {
                videos.add(video);
            }
        }
        
        savedTopic.setVideos(videos);
        
        // Generate quizzes based on video transcripts
        if (!videos.isEmpty()) {
            generateQuizzesForTopic(savedTopic, videos);
        }
        
        return savedTopic;
    }
    
    /**
     * Process a single video: save details and get transcript
     */
    private Video processVideo(Topic topic, Map<String, Object> videoData) {
        try {
            String videoId = (String) videoData.get("videoId");
            
            // Check if video already exists
            if (videoRepository.findByYoutubeVideoId(videoId).isPresent()) {
                log.debug("Video {} already exists, skipping", videoId);
                return null;
            }
            
            // Get detailed video information
            Map<String, Object> videoDetails = youTubeApiService.getVideoDetails(videoId);
            
            Video video = new Video();
            video.setTitle((String) videoData.get("title"));
            video.setUrl((String) videoData.get("url"));
            video.setYoutubeVideoId(videoId);
            video.setCaptionAvailable(true); // We only process videos with captions
            video.setTopic(topic);
            
            // Set statistics
            if (videoData.containsKey("viewCount")) {
                video.setViewCount((Long) videoData.get("viewCount"));
            }
            if (videoData.containsKey("likeCount")) {
                video.setLikeCount((Long) videoData.get("likeCount"));
            }
            if (videoData.containsKey("durationMinutes")) {
                video.setDurationMinutes((Integer) videoData.get("durationMinutes"));
            }
            
            Video savedVideo = videoRepository.save(video);
            
            // Get transcript
            String transcript = transcriptService.getVideoTranscript(videoId);
            if (transcript != null && !transcript.trim().isEmpty()) {
                String cleanTranscript = transcriptService.cleanTranscript(transcript);
                savedVideo.setTranscript(cleanTranscript);
                videoRepository.save(savedVideo);
            }
            
            log.debug("Processed video: {}", savedVideo.getTitle());
            return savedVideo;
            
        } catch (Exception e) {
            log.error("Error processing video: {}", videoData.get("videoId"), e);
            return null;
        }
    }
    
    /**
     * Generate quizzes for a topic based on video transcripts
     */
    private void generateQuizzesForTopic(Topic topic, List<Video> videos) {
        try {
            // Combine transcripts from all videos
            String combinedTranscript = videos.stream()
                    .map(Video::getTranscript)
                    .filter(transcript -> transcript != null && !transcript.trim().isEmpty())
                    .collect(Collectors.joining(" "));
            
            if (combinedTranscript.trim().isEmpty()) {
                log.warn("No transcripts available for topic: {}", topic.getName());
                return;
            }
            
            // Generate quiz questions
            List<Map<String, Object>> quizQuestions = groqApiService.generateQuizQuestions(
                    combinedTranscript, topic.getName());
            
            List<Quiz> quizzes = new ArrayList<>();
            for (Map<String, Object> questionData : quizQuestions) {
                Quiz quiz = new Quiz();
                quiz.setQuestion((String) questionData.get("question"));
                quiz.setCorrectAnswer((String) questionData.get("correctAnswer"));
                quiz.setTopic(topic);
                
                // Convert options to JSON string
                try {
                    List<String> options = (List<String>) questionData.get("options");
                    quiz.setOptions(objectMapper.writeValueAsString(options));
                } catch (JsonProcessingException e) {
                    log.error("Error serializing quiz options", e);
                    continue;
                }
                
                quizzes.add(quiz);
            }
            
            quizRepository.saveAll(quizzes);
            log.info("Generated {} quiz questions for topic: {}", quizzes.size(), topic.getName());
            
        } catch (Exception e) {
            log.error("Error generating quizzes for topic: {}", topic.getName(), e);
        }
    }
    
    /**
     * Get or create user for demo purposes
     */
    private User getOrCreateUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        }
        
        // Create a default demo user
        User user = new User();
        user.setUsername("demo_user");
        user.setEmail("demo@learnnow.com");
        return userRepository.save(user);
    }
    
    /**
     * Map Course entity to response DTO
     */
    private CourseResponseDto mapToResponseDto(Course course) {
        CourseResponseDto dto = new CourseResponseDto();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getName());
        dto.setCourseDescription(course.getDescription());
        dto.setSummary(course.getSummary());
        dto.setCreatedAt(course.getCreatedAt());
        
        List<CourseResponseDto.TopicDto> topicDtos = course.getTopics().stream()
                .map(this::mapTopicToDto)
                .collect(Collectors.toList());
        dto.setTopics(topicDtos);
        
        return dto;
    }
    
    /**
     * Map Topic entity to DTO
     */
    private CourseResponseDto.TopicDto mapTopicToDto(Topic topic) {
        CourseResponseDto.TopicDto dto = new CourseResponseDto.TopicDto();
        dto.setTopicId(topic.getId());
        dto.setTopicName(topic.getName());
        dto.setDescription(topic.getDescription());
        
        List<CourseResponseDto.VideoDto> videoDtos = topic.getVideos().stream()
                .map(this::mapVideoToDto)
                .collect(Collectors.toList());
        dto.setVideos(videoDtos);
        
        List<CourseResponseDto.QuizDto> quizDtos = topic.getQuizzes().stream()
                .map(this::mapQuizToDto)
                .collect(Collectors.toList());
        dto.setQuizzes(quizDtos);
        
        return dto;
    }
    
    /**
     * Map Video entity to DTO
     */
    private CourseResponseDto.VideoDto mapVideoToDto(Video video) {
        CourseResponseDto.VideoDto dto = new CourseResponseDto.VideoDto();
        dto.setVideoId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setUrl(video.getUrl());
        dto.setYoutubeVideoId(video.getYoutubeVideoId());
        dto.setCaptionAvailable(video.getCaptionAvailable());
        dto.setDurationMinutes(video.getDurationMinutes());
        dto.setViewCount(video.getViewCount());
        dto.setLikeCount(video.getLikeCount());
        return dto;
    }
    
    /**
     * Map Quiz entity to DTO
     */
    private CourseResponseDto.QuizDto mapQuizToDto(Quiz quiz) {
        CourseResponseDto.QuizDto dto = new CourseResponseDto.QuizDto();
        dto.setQuizId(quiz.getId());
        dto.setQuestion(quiz.getQuestion());
        dto.setCorrectAnswer(quiz.getCorrectAnswer());
        dto.setDifficultyLevel(quiz.getDifficultyLevel());
        
        // Parse options from JSON string
        try {
            List<String> options = objectMapper.readValue(quiz.getOptions(), List.class);
            dto.setOptions(options);
        } catch (JsonProcessingException e) {
            log.error("Error parsing quiz options", e);
            dto.setOptions(List.of("Option A", "Option B", "Option C", "Option D"));
        }
        
        return dto;
    }
}