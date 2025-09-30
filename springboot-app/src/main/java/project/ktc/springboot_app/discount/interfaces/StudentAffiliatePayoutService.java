package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutResponseDto;
import project.ktc.springboot_app.discount.dto.StudentAffiliatePayoutStatisticsDto;

/** Service interface for student affiliate payout operations */
public interface StudentAffiliatePayoutService {

	/**
	 * Get all affiliate payouts for the student (commissions they receive)
	 *
	 * @param pageable
	 *            Pagination parameters
	 * @return ResponseEntity containing paginated list of affiliate payouts
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<StudentAffiliatePayoutResponseDto>>> getAffiliatePayouts(
			Pageable pageable);

	/**
	 * Get affiliate payout statistics for the student
	 *
	 * @return ResponseEntity containing affiliate payout statistics
	 */
	ResponseEntity<ApiResponse<StudentAffiliatePayoutStatisticsDto>> getAffiliatePayoutStatistics();
}
