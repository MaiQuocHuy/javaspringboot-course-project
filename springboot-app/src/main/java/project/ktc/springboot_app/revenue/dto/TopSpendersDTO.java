package project.ktc.springboot_app.revenue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSpendersDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StudentSpendingData {
    private String id;
    private String name;
    private String email;
    private Double totalSpent;
    private Integer coursesEnrolled;
    private String avatarUrl;
  }

  private List<StudentSpendingData> topStudents;
  private Integer limit;
}