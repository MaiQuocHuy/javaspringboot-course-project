package project.ktc.springboot_app.common.exception;

/**
 * Exception thrown when user is not eligible to submit instructor application
 */
public class IneligibleApplicationException extends RuntimeException {
    public IneligibleApplicationException(String message) {
        super(message);
    }
}
