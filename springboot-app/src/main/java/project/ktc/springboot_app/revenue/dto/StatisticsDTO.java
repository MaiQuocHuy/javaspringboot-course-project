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
public class StatisticsDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StatCard {
    private String title;
    private String value;
  }

  private List<StatCard> stats;
  private Double totalRevenue;
  private Double monthlyGrowth;
  private Double yearlyGrowth;
  private Double avgRevenuePerUser;
  private Long totalActiveUsers;
  private Long totalTransactions;
}