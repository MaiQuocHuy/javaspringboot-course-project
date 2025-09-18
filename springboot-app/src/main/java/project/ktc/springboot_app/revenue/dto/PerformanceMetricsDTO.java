package project.ktc.springboot_app.revenue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetricsDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryRevenue {
    private String category;
    private Double revenue;
    private Integer studentsCount;
    private Integer coursesCount;
    private Double percentage;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InstructorPerformance {
    private String id;
    private String name;
    private String email;
    private Integer coursesCount;
    private Double totalRevenue;
    private Double averageRating;
    private Integer totalStudents;
  }

  private List<CategoryRevenue> categoryRevenues;
  private Map<String, Double> revenueByCategory;
  private List<InstructorPerformance> topInstructors;
  private String topPerformingCategory;
  private String worstPerformingCategory;
  // private Double averageRevenuePerUser;
  // private Long totalActiveUsers;
}