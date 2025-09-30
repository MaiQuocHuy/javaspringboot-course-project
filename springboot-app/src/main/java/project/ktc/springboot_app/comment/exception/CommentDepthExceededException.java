package project.ktc.springboot_app.comment.exception;

import project.ktc.springboot_app.common.exception.BusinessLogicException;

public class CommentDepthExceededException extends BusinessLogicException {

	public CommentDepthExceededException() {
		super("Comment depth limit exceeded. Maximum 3 levels allowed.");
	}

	public CommentDepthExceededException(String message) {
		super(message);
	}
}
