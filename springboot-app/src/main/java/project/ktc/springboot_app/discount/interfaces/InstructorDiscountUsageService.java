package project.ktc.springboot_app.discount.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.discount.dto.InstructorDiscountUsageResponseDto;

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
}
