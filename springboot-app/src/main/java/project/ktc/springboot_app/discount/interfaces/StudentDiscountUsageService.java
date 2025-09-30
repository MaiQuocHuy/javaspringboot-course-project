package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.StudentDiscountUsageResponseDto;

/** Service interface for student discount usage operations */
public interface StudentDiscountUsageService {

  /**
   * Get all discount usages where the current student is the referrer (People using the student's
   * discount code)
   *
   * @param pageable Pagination parameters
   * @return ResponseEntity containing paginated list of discount usages
   */
  ResponseEntity<ApiResponse<PaginatedResponse<StudentDiscountUsageResponseDto>>> getDiscountUsages(
      Pageable pageable);
}
