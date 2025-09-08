package com.learnnow.dreampipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for LEARNnow Adaptive Learning System
 * 
 * This system processes course descriptions into complete learning journeys
 * with videos, quizzes, and performance analytics.
 */
@SpringBootApplication
public class DreamPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreamPipelineApplication.class, args);
    }
}