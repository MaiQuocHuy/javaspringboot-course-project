package project.ktc.springboot_app.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Global API Response wrapper for consistent response structure
 * 
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * HTTP status code (e.g., 200, 201, 400, 404, 500)
     */
    private int statusCode;
    
    /**
     * Human-readable description of the response outcome
     */
    private String message;
    
    /**
     * Response body for successful results; null for errors
     */
    private T data;
    
    /**
     * Server response time in ISO 8601 format
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;
    
    /**
     * Constructor for success responses with data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }
    
    /**
     * Constructor for success responses without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .message(message)
                .data(null)
                .timestamp(ZonedDateTime.now())
                .build();
    }
    
    /**
     * Constructor for created responses
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(201)
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }
    
    /**
     * Constructor for error responses
     */
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .data(null)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}
