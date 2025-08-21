package project.ktc.springboot_app.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRefundStatusDto {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(COMPLETED|FAILED)$", message = "Status must be either 'COMPLETED' or 'FAILED'")
    private String status;

    private String rejectedReason;
}
