package project.ktc.springboot_app.security.exception;

/** Exception thrown when JWT token is expired */
public class ExpiredJwtTokenException extends RuntimeException {
  public ExpiredJwtTokenException(String message) {
    super(message);
  }

  public ExpiredJwtTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
