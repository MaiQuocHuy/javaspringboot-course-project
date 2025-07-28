package project.ktc.springboot_app.upload.exception;

/**
 * Exception thrown when document validation fails
 */
public class InvalidDocumentFormatException extends RuntimeException {
    public InvalidDocumentFormatException(String message) {
        super(message);
    }
}
