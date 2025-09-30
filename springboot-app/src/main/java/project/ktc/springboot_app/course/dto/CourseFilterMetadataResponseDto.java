package project.ktc.springboot_app.course.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseFilterMetadataResponseDto {
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
}
