package project.ktc.springboot_app.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaidOutResponseDto {

    private String paymentId;
    private String courseId;
    private String courseTitle;
    private String instructorId;
    private String instructorName;
    private BigDecimal amount;
    private BigDecimal instructorEarning;
    private LocalDateTime paidOutAt;
    private String earningId;

    private String message;
}
