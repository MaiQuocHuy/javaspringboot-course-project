package project.ktc.springboot_app.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO for student payment statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student payment statistics containing comprehensive payment metrics")
public class StudentPaymentStatsDto {

    @Schema(description = "Total number of payments made by the student", example = "8")
    private Long totalPayments;

    @Schema(description = "Total amount spent by the student across all payments", example = "599.92")
    private BigDecimal totalAmountSpent;

    @Schema(description = "Number of successful/completed payments", example = "7")
    private Long completedPayments;

    @Schema(description = "Number of failed payments", example = "1")
    private Long failedPayments;
}