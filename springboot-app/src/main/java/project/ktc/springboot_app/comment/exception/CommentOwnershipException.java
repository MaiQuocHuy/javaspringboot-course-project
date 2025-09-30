package project.ktc.springboot_app.comment.exception;

import project.ktc.springboot_app.common.exception.BusinessLogicException;

public class CommentOwnershipException extends BusinessLogicException {

	public CommentOwnershipException() {
		super("You do not have permission to modify this comment.");
	}

	public CommentOwnershipException(String message) {
		super(message);
	}
}
