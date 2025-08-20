package project.ktc.springboot_app.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.payment.entity.Payment.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating payment status by admin")
public class AdminUpdatePaymentStatusDto {

    @NotNull
    @Schema(description = "New status of the payment")
    private PaymentStatus status;

}
