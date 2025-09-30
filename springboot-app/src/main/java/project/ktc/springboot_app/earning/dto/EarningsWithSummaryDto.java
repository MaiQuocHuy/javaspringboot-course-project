package project.ktc.springboot_app.earning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsWithSummaryDto {
  private PaginatedResponse<EarningResponseDto> earnings;
  private EarningSummaryDto summary;
}
