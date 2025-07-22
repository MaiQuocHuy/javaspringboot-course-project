package project.ktc.springboot_app.upload.exception;

/**
 * Exception thrown when uploaded file is not a valid image
 */
public class InvalidImageFormatException extends RuntimeException {

    public InvalidImageFormatException(String message) {
        super(message);
    }

    public InvalidImageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
