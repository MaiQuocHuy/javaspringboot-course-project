package project.ktc.springboot_app.instructor_dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsDashboardDto {
  private StatisticItemDto courseStatistics;
  private StatisticItemDto studentStatistics;
  private StatisticItemDto revenueStatistics;
  private StatisticItemDto ratingStatistics;
}
