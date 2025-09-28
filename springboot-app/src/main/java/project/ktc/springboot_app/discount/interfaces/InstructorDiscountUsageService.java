package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.InstructorDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.enums.DiscountType;

/**
 * Service interface for instructor discount usage operations
 */
public interface InstructorDiscountUsageService {

    /**
     * Get all discount usages for courses owned by the instructor
     * 
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of discount usages
     */
    ResponseEntity<ApiResponse<PaginatedResponse<InstructorDiscountUsageResponseDto>>> getDiscountUsages(
            Pageable pageable);

    /**
     * Get all discount usages for courses owned by the instructor with search and
     * filtering
     * 
     * @param search   Search by discount usage ID, discount code, user name, or
     *                 course title
     * @param type     Filter by discount type
     * @param fromDate Filter by usage date from (ISO date string)
     * @param toDate   Filter by usage date to (ISO date string)
     * @param pageable Pagination parameters
     * @return ResponseEntity containing paginated list of filtered discount usages
     */
    ResponseEntity<ApiResponse<PaginatedResponse<InstructorDiscountUsageResponseDto>>> getDiscountUsages(
            String search, DiscountType type, String fromDate, String toDate, Pageable pageable);

    /**
     * Get a specific discount usage by ID for courses owned by the instructor
     * 
     * @param discountUsageId The ID of the discount usage
     * @return ResponseEntity containing the discount usage details
     */
    ResponseEntity<ApiResponse<InstructorDiscountUsageResponseDto>> getDiscountUsageById(String discountUsageId);
}
