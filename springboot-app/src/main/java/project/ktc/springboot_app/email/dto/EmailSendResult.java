package project.ktc.springboot_app.email.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email send result DTO */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResult {

	private boolean success;
	private String messageId;
	private String errorMessage;
	private LocalDateTime sentAt;
	private String provider;
	private int attemptCount;

	public static EmailSendResult success(String messageId, String provider) {
		return EmailSendResult.builder()
				.success(true)
				.messageId(messageId)
				.sentAt(LocalDateTime.now())
				.provider(provider)
				.attemptCount(1)
				.build();
	}

	public static EmailSendResult failure(String errorMessage, String provider, int attemptCount) {
		return EmailSendResult.builder()
				.success(false)
				.errorMessage(errorMessage)
				.provider(provider)
				.attemptCount(attemptCount)
				.build();
	}

	public static EmailSendResult failure(String errorMessage, String provider) {
		return failure(errorMessage, provider, 1);
	}
}
