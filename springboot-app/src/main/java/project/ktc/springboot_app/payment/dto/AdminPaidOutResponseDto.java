package project.ktc.springboot_app.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
