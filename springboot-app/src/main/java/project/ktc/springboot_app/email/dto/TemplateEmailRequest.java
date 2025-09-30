package project.ktc.springboot_app.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Template email request DTO for sending emails with templates */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateEmailRequest {

	@NotBlank(message = "Recipient email is required")
	@Email(message = "Invalid email format")
	private String to;

	@NotBlank(message = "Subject is required")
	private String subject;

	@NotBlank(message = "Template name is required")
	private String templateName;

	@NotNull(message = "Template variables are required")
	@Builder.Default
	private Map<String, Object> templateVariables = Map.of();

	@Builder.Default
	private boolean async = true;
}
