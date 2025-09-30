package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.refund.entity.Refund.RefundStatus;

@Getter
@Setter
@Builder
public class RefundResponseDto {
  private String id;
  private CourseInfo course;
  private String reason;
  private RefundStatus status;
  private BigDecimal amount;
  private LocalDateTime requestedAt;

  @Getter
  @Setter
  @Builder
  public static class CourseInfo {
    private String id;
    private String title;
  }
}
