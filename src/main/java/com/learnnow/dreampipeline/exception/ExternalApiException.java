package com.learnnow.dreampipeline.exception;

/**
 * Custom exception for external API errors
 */
public class ExternalApiException extends RuntimeException {
    
    private final String apiName;
    private final int statusCode;
    
    public ExternalApiException(String apiName, int statusCode, String message) {
        super(String.format("%s API error (status %d): %s", apiName, statusCode, message));
        this.apiName = apiName;
        this.statusCode = statusCode;
    }
    
    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(String.format("%s API error: %s", apiName, message), cause);
        this.apiName = apiName;
        this.statusCode = -1;
    }
    
    public String getApiName() {
        return apiName;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}