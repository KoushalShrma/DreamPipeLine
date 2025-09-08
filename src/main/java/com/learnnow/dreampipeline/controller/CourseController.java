package com.learnnow.dreampipeline.controller;

import com.learnnow.dreampipeline.dto.CourseRequestDto;
import com.learnnow.dreampipeline.dto.CourseResponseDto;
import com.learnnow.dreampipeline.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for course management
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {
    
    private final CourseService courseService;
    
    /**
     * Create a new course and trigger the complete learning pipeline
     * 
     * @param request Course creation request with name and description
     * @return Created course with topics, videos, and quizzes
     */
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto request) {
        log.info("Creating course: {}", request.getCourseName());
        
        CourseResponseDto response = courseService.createCourse(request);
        
        log.info("Course created successfully with ID: {}", response.getCourseId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get course details by ID
     * 
     * @param courseId The course ID
     * @return Course details with topics, videos, and quizzes
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long courseId) {
        log.info("Retrieving course with ID: {}", courseId);
        
        CourseResponseDto response = courseService.getCourseById(courseId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all courses for a specific user
     * 
     * @param userId The user ID
     * @return List of courses for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByUserId(@PathVariable Long userId) {
        log.info("Retrieving courses for user ID: {}", userId);
        
        List<CourseResponseDto> courses = courseService.getCoursesByUserId(userId);
        
        return ResponseEntity.ok(courses);
    }
}