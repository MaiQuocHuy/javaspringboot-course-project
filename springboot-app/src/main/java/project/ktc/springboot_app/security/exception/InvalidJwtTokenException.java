package project.ktc.springboot_app.security.exception;

/**
 * Exception thrown when JWT token is invalid
 */
public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }

    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
