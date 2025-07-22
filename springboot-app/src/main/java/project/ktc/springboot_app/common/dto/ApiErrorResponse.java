package project.ktc.springboot_app.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * API Error Response for detailed error information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    
    /**
     * HTTP status code
     */
    private int statusCode;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Error type/category
     */
    private String error;
    
    /**
     * Detailed error information (validation errors, field errors, etc.)
     */
    private Object details;
    
    /**
     * Request path where error occurred
     */
    private String path;
    
    /**
     * Server response time in ISO 8601 format
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;
    
    /**
     * Create a simple error response
     */
    public static ApiErrorResponse of(int statusCode, String error, String message, String path) {
        return ApiErrorResponse.builder()
                .statusCode(statusCode)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(ZonedDateTime.now())
                .build();
    }
    
    /**
     * Create an error response with details
     */
    public static ApiErrorResponse of(int statusCode, String error, String message, Object details, String path) {
        return ApiErrorResponse.builder()
                .statusCode(statusCode)
                .error(error)
                .message(message)
                .details(details)
                .path(path)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}
