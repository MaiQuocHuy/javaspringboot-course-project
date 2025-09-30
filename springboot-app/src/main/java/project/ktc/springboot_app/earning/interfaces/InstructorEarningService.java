package project.ktc.springboot_app.earning.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.earning.dto.EarningDetailResponseDto;
import project.ktc.springboot_app.earning.dto.EarningsWithSummaryDto;
import project.ktc.springboot_app.earning.dto.MonthlyEarningsDto;

public interface InstructorEarningService {

  ResponseEntity<ApiResponse<EarningsWithSummaryDto>> getEarnings(
      String courseId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

  ResponseEntity<ApiResponse<EarningDetailResponseDto>> getEarningDetails(String earningId);

  ResponseEntity<ApiResponse<List<MonthlyEarningsDto>>> getRecentRevenues();
}
