package project.ktc.springboot_app.common.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;

/**
 * Utility class for creating consistent API responses
 */
public class ApiResponseUtil {

    /**
     * Create a successful response with data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Create a successful response without data
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * Create a created response
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, message));
    }

    /**
     * Create a created response without data
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<T>builder()
                        .statusCode(201)
                        .message(message)
                        .data(null)
                        .timestamp(java.time.ZonedDateTime.now())
                        .build());
    }

    /**
     * Create a bad request response
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, message));
    }

    /**
     * Create an unauthorized response
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, message));
    }

    /**
     * Create a forbidden response
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, message));
    }

    /**
     * Create a not found response
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, message));
    }

    /**
     * Create a conflict response
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, message));
    }

    /**
     * Create an internal server error response
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, message));
    }

    /**
     * Create a custom error response
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), message));
    }
}
