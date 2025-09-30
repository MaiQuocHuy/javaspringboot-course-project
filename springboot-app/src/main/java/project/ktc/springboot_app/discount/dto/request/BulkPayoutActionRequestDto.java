package project.ktc.springboot_app.discount.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** Request DTO for bulk payout actions */
@Data
public class BulkPayoutActionRequestDto {

	@NotEmpty(message = "Payout IDs cannot be empty")
	private List<String> payoutIds;

	@NotNull(message = "Action is required")
	private BulkPayoutAction action;

	private String reason; // Optional reason for cancellation

	public enum BulkPayoutAction {
		MARK_PAID, CANCEL
	}
}
