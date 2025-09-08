package com.learnnow.dreampipeline.service;

import com.learnnow.dreampipeline.config.AppConfig;
import com.learnnow.dreampipeline.dto.CourseRequestDto;
import com.learnnow.dreampipeline.dto.CourseResponseDto;
import com.learnnow.dreampipeline.entity.Course;
import com.learnnow.dreampipeline.entity.Topic;
import com.learnnow.dreampipeline.entity.User;
import com.learnnow.dreampipeline.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TopicRepository topicRepository;
    
    @Mock
    private VideoRepository videoRepository;
    
    @Mock
    private QuizRepository quizRepository;
    
    @Mock
    private GroqApiService groqApiService;
    
    @Mock
    private YouTubeApiService youTubeApiService;
    
    @Mock
    private YouTubeTranscriptService transcriptService;
    
    @InjectMocks
    private CourseService courseService;
    
    private User testUser;
    private Course testCourse;
    private CourseRequestDto testRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");
        testCourse.setDescription("Test course description");
        testCourse.setUser(testUser);
        
        testRequest = new CourseRequestDto();
        testRequest.setCourseName("Test Course");
        testRequest.setCourseDescription("Test course description for learning");
        testRequest.setUserId(1L);
    }
    
    @Test
    void testCreateCourse_Success() {
        // Arrange
        Topic mockTopic = new Topic();
        mockTopic.setId(1L);
        mockTopic.setName("Topic 1");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(groqApiService.generateCourseSummary(any())).thenReturn("Test summary");
        when(groqApiService.generateSubtopics(any())).thenReturn(List.of("Topic 1", "Topic 2"));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(topicRepository.save(any())).thenReturn(mockTopic);
        when(youTubeApiService.searchVideosWithCaptions(any())).thenReturn(List.of());
        
        // Act
        CourseResponseDto result = courseService.createCourse(testRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Course", result.getCourseName());
        verify(courseRepository).save(any(Course.class));
        verify(groqApiService).generateCourseSummary(testRequest.getCourseDescription());
        verify(groqApiService).generateSubtopics("Test summary");
    }
    
    @Test
    void testGetCourseById_Success() {
        // Arrange
        when(courseRepository.findByIdWithTopicsAndDetails(1L)).thenReturn(Optional.of(testCourse));
        
        // Act
        CourseResponseDto result = courseService.getCourseById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        assertEquals("Test Course", result.getCourseName());
    }
    
    @Test
    void testGetCoursesByUserId_Success() {
        // Arrange
        when(courseRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(testCourse));
        
        // Act
        List<CourseResponseDto> result = courseService.getCoursesByUserId(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Course", result.get(0).getCourseName());
    }
}