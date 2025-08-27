package project.ktc.springboot_app.chat.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Response for chat messages list endpoint")
public class ChatMessagesListResponse {

    @Schema(description = "List of chat messages")
    private List<SimpleChatMessageResponse> messages;

    @Schema(description = "Current page number", example = "0")
    private Integer page;

    @Schema(description = "Number of messages per page", example = "50")
    private Integer size;

    @Schema(description = "Total number of messages", example = "100")
    private Long totalElements;

    @Schema(description = "Total number of pages", example = "2")
    private Integer totalPages;
}
