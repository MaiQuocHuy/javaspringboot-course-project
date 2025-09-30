package project.ktc.springboot_app.log.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** DTO for payment audit trail logging */
@Data
@Builder
public class PaymentLogDto {
  private String id;
  private String userId;
  private String courseId;
  private BigDecimal amount;
  private String status;
  private String paymentMethod;
  private String sessionId;
  private LocalDateTime paidAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Related entity information for audit context
  private UserLogDto user;
  private CourseLogDto course;

  @Data
  @Builder
  public static class UserLogDto {
    private String id;
    private String name;
    private String email;
  }

  @Data
  @Builder
  public static class CourseLogDto {
    private String id;
    private String title;
    private BigDecimal price;
    private String instructorId;
    private String instructorName;
  }
}
