package project.ktc.springboot_app.discount.dto.request;

import lombok.Data;

/** Request DTO for cancelling a payout */
@Data
public class CancelPayoutRequestDto {

	private String reason; // Optional reason for cancellation
}
