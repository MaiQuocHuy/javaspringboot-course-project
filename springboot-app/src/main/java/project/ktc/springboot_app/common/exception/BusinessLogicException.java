package project.ktc.springboot_app.common.exception;

/**
 * Exception thrown when business logic rules are violated.
 * 
 * This exception is used to indicate that an operation failed due to
 * business rule violations, such as:
 * - Rate limiting
 * - State constraints
 * - Business process violations
 * - Policy enforcement failures
 * 
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
public class BusinessLogicException extends RuntimeException {

    /**
     * Constructs a new business logic exception with null as its detail message.
     */
    public BusinessLogicException() {
        super();
    }

    /**
     * Constructs a new business logic exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public BusinessLogicException(String message) {
        super(message);
    }

    /**
     * Constructs a new business logic exception with the specified detail message
     * and cause.
     * 
     * @param message the detail message
     * @param cause   the cause
     */
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new business logic exception with the specified cause.
     * 
     * @param cause the cause
     */
    public BusinessLogicException(Throwable cause) {
        super(cause);
    }
}
