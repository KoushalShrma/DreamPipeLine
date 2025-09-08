package com.learnnow.dreampipeline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for course creation request
 */
@Data
public class CourseRequestDto {
    
    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String courseName;
    
    @NotBlank(message = "Course description is required")
    @Size(min = 10, max = 2000, message = "Course description must be between 10 and 2000 characters")
    private String courseDescription;
    
    private Long userId; // Optional - can be derived from authentication context
}