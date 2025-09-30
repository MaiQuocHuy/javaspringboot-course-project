package project.ktc.springboot_app.common.exception;

public class UserNotEnrolledException extends RuntimeException {
	public UserNotEnrolledException(String message) {
		super(message);
	}
}
