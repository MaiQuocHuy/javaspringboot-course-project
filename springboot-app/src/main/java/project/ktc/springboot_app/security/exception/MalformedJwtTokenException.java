package project.ktc.springboot_app.security.exception;

/** Exception thrown when JWT token is malformed */
public class MalformedJwtTokenException extends RuntimeException {
	public MalformedJwtTokenException(String message) {
		super(message);
	}

	public MalformedJwtTokenException(String message, Throwable cause) {
		super(message, cause);
	}
}
