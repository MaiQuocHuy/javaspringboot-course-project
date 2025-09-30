package project.ktc.springboot_app.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Email request DTO containing all necessary information for sending emails */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

	@NotEmpty(message = "At least one recipient is required")
	private List<String> to;

	private List<String> cc;

	private List<String> bcc;

	private String from;

	private String replyTo;

	@NotNull(message = "Subject is required")
	private String subject;

	private String htmlBody;

	private String plainTextBody;

	private String templateName;

	@Builder.Default
	private Map<String, Object> templateVariables = Map.of();

	private List<EmailAttachment> attachments;

	private List<EmailInlineImage> inlineImages;

	@Builder.Default
	private boolean async = true;

	@Builder.Default
	private boolean sendAfterCommit = false;

	@Builder.Default
	private EmailPriority priority = EmailPriority.NORMAL;

	public enum EmailPriority {
		LOW, NORMAL, HIGH
	}
}
