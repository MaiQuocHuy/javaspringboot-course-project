package project.ktc.springboot_app.common.exception;

public class ReviewAlreadyExistsException extends RuntimeException {
  public ReviewAlreadyExistsException(String message) {
    super(message);
  }
}
