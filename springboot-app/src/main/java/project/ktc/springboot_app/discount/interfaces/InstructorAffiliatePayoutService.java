package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.InstructorAffiliatePayoutResponseDto;

/**
 * Service interface for instructor affiliate payout operations
 */
public interface InstructorAffiliatePayoutService {

        /**
         * Get all affiliate payouts for courses owned by the instructor
         * (Shows who gets commission on instructor's courses)
         * 
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of affiliate payouts
         */
        ResponseEntity<ApiResponse<PaginatedResponse<InstructorAffiliatePayoutResponseDto>>> getAffiliatePayouts(
                        Pageable pageable);

        /**
         * Get all affiliate payouts for courses owned by the instructor with search and
         * filter options
         * (Shows who gets commission on instructor's courses)
         * 
         * @param search   Search term for affiliate payout ID, referrer name, course
         *                 title, or discount code
         * @param status   Filter by payout status
         * @param fromDate Filter by payout date from
         * @param toDate   Filter by payout date to
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of affiliate payouts
         */
        ResponseEntity<ApiResponse<PaginatedResponse<InstructorAffiliatePayoutResponseDto>>> getAffiliatePayouts(
                        String search, PayoutStatus status, String fromDate, String toDate, Pageable pageable);

        /**
         * Get a specific affiliate payout by ID for courses owned by the instructor
         * 
         * @param affiliatePayoutId The ID of the affiliate payout
         * @return ResponseEntity containing the affiliate payout details
         */
        ResponseEntity<ApiResponse<InstructorAffiliatePayoutResponseDto>> getAffiliatePayoutById(
                        String affiliatePayoutId);
}
