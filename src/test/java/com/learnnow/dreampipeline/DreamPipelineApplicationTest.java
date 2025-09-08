package com.learnnow.dreampipeline;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify Spring Boot application context loads correctly
 */
@SpringBootTest
@ActiveProfiles("test")
class DreamPipelineApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads without errors
        // If any beans have configuration issues, this test will fail
    }
}