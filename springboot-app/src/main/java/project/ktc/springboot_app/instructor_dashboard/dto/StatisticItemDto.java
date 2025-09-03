package project.ktc.springboot_app.instructor_dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticItemDto {
  private String title;
  private String value;
  private String description;
}
