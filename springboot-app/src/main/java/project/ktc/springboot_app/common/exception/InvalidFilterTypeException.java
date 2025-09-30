package project.ktc.springboot_app.common.exception;

/** Thrown when a provided filter type identifier (ID or name) cannot be resolved. */
public class InvalidFilterTypeException extends RuntimeException {
  public InvalidFilterTypeException(String message) {
    super(message);
  }
}
