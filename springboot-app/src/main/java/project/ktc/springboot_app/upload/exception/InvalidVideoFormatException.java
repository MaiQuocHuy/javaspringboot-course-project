package project.ktc.springboot_app.upload.exception;

/**
 * Exception thrown when video file validation fails
 */
public class InvalidVideoFormatException extends RuntimeException {

    public InvalidVideoFormatException(String message) {
        super(message);
    }

    public InvalidVideoFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
