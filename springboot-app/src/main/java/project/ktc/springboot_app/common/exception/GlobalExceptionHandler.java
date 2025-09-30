package project.ktc.springboot_app.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.security.exception.ExpiredJwtTokenException;
import project.ktc.springboot_app.security.exception.InvalidJwtTokenException;
import project.ktc.springboot_app.security.exception.MalformedJwtTokenException;
import project.ktc.springboot_app.upload.exception.ImageUploadException;
import project.ktc.springboot_app.upload.exception.InvalidImageFormatException;

/** Global exception handler for consistent error responses */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /** Handle validation errors (Bean Validation) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    log.error("Validation error occurred: {}", ex.getMessage());

    List<Map<String, String>> fieldErrors = new ArrayList<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      Map<String, String> error = new HashMap<>();
      error.put("field", fieldError.getField());
      error.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
      error.put("message", fieldError.getDefaultMessage());
      fieldErrors.add(error);
    }

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "Validation failed for multiple fields",
            fieldErrors,
            request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle entity not found errors */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
      EntityNotFoundException ex, HttpServletRequest request) {

    log.error("Entity not found: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /** Handle authentication errors */
  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {

    log.error("Authentication error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid credentials",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /** Handle authorization errors */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {

    log.error("Access denied: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "Access denied: insufficient permissions",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /** Handle database constraint violations */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    log.error("Data integrity violation: {}", ex.getMessage());

    String message = "Data integrity violation";
    if (ex.getMessage().contains("email")) {
      message = "Email already exists";
    } else if (ex.getMessage().contains("unique")) {
      message = "Duplicate entry detected";
    }

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", message, request.getRequestURI());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /** Handle illegal argument exceptions */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {

    log.error("Illegal argument: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle validation exceptions (business logic validation) */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(
      ValidationException ex, HttpServletRequest request) {

    log.error("Validation exception: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle illegal state exceptions */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalState(
      IllegalStateException ex, HttpServletRequest request) {

    log.error("Illegal state: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle method argument type mismatch */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    log.error("Type mismatch error: {}", ex.getMessage());

    String message =
        String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", message, request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle runtime exceptions (business logic errors) */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {

    log.error("Runtime exception: {}", ex.getMessage(), ex);

    // Check for specific business logic errors
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String error = "Internal Server Error";

    if (ex.getMessage().contains("already exists")
        || ex.getMessage().contains("already enrolled")) {
      status = HttpStatus.CONFLICT;
      error = "Conflict";
    } else if (ex.getMessage().contains("not found")) {
      status = HttpStatus.NOT_FOUND;
      error = "Not Found";
    } else if (ex.getMessage().contains("Invalid") || ex.getMessage().contains("cannot be")) {
      status = HttpStatus.BAD_REQUEST;
      error = "Bad Request";
    }

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(status.value(), error, ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(status).body(errorResponse);
  }

  /** Handle image upload errors */
  @ExceptionHandler(ImageUploadException.class)
  public ResponseEntity<ApiErrorResponse> handleImageUploadException(
      ImageUploadException ex, HttpServletRequest request) {

    log.error("Image upload error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Upload Failed",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /** Handle invalid image format errors */
  @ExceptionHandler(InvalidImageFormatException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidImageFormat(
      InvalidImageFormatException ex, HttpServletRequest request) {

    log.error("Invalid image format: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid File Format",
            ex.getMessage(),
            request.getRequestURI());

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle JWT token related exceptions */
  @ExceptionHandler({ExpiredJwtTokenException.class})
  public ResponseEntity<ApiErrorResponse> handleExpiredJwtToken(
      ExpiredJwtTokenException ex, HttpServletRequest request) {
    log.error("JWT token expired: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "JWT_TOKEN_EXPIRED",
            "JWT token has expired. Please obtain a new token.",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler({MalformedJwtTokenException.class})
  public ResponseEntity<ApiErrorResponse> handleMalformedJwtToken(
      MalformedJwtTokenException ex, HttpServletRequest request) {
    log.error("JWT token malformed: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "JWT_TOKEN_MALFORMED",
            "JWT token is malformed. Please provide a valid token.",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler({InvalidJwtTokenException.class})
  public ResponseEntity<ApiErrorResponse> handleInvalidJwtToken(
      InvalidJwtTokenException ex, HttpServletRequest request) {
    log.error("JWT token invalid: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "JWT_TOKEN_INVALID",
            "JWT token is invalid. Please provide a valid token.",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /** Handle resource not found exceptions */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {

    log.error("Resource not found: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /** Handle comment-specific exceptions */
  @ExceptionHandler(
      project.ktc.springboot_app.comment.exception.CommentDepthExceededException.class)
  public ResponseEntity<ApiErrorResponse> handleCommentDepthExceeded(
      project.ktc.springboot_app.comment.exception.CommentDepthExceededException ex,
      HttpServletRequest request) {

    log.error("Comment depth exceeded error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  @ExceptionHandler(project.ktc.springboot_app.comment.exception.CommentOwnershipException.class)
  public ResponseEntity<ApiErrorResponse> handleCommentOwnership(
      project.ktc.springboot_app.comment.exception.CommentOwnershipException ex,
      HttpServletRequest request) {

    log.error("Comment ownership error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /** Handle review exceptions */
  @ExceptionHandler(project.ktc.springboot_app.common.exception.ReviewAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleReviewAlreadyExists(
      project.ktc.springboot_app.common.exception.ReviewAlreadyExistsException ex,
      HttpServletRequest request) {

    log.error("Review already exists error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  @ExceptionHandler(project.ktc.springboot_app.common.exception.UserNotEnrolledException.class)
  public ResponseEntity<ApiErrorResponse> handleUserNotEnrolled(
      project.ktc.springboot_app.common.exception.UserNotEnrolledException ex,
      HttpServletRequest request) {

    log.error("User not enrolled error: {}", ex.getMessage());

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /** Handle all other exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {

    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

    ApiErrorResponse errorResponse =
        ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
