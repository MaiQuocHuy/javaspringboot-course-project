package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for async message creation acknowledgment")
public class AsyncMessageAcknowledgment {

	@Schema(description = "Temporary ID from the request", example = "temp-123")
	private String tempId;

	@Schema(description = "Status of the message", example = "PENDING", allowableValues = { "PENDING" })
	private String status;
}
