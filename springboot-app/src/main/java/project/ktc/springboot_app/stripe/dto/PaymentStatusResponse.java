package project.ktc.springboot_app.stripe.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

  private String id;
  private String sessionId;
  private String courseId;
  private String userId;
  private Double amount;
  private String currency;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private CourseInfo course;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseInfo {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private Double price;
  }
}
