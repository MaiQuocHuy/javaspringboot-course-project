package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request object for updating a chat message")
public class UpdateMessageRequest {

    @NotBlank(message = "Message type is required")
    @Schema(description = "Type of message (only TEXT messages can be updated)", example = "TEXT", allowableValues = {
            "TEXT" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 1000, message = "Message content must be between 1 and 1000 characters")
    @Schema(description = "Updated message content", example = "Updated message content here", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
