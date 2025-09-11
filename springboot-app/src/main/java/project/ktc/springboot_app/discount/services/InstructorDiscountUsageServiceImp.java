package project.ktc.springboot_app.discount.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.discount.dto.InstructorDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.interfaces.InstructorDiscountUsageService;
import project.ktc.springboot_app.discount.repositories.InstructorDiscountUsageRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

/**
 * Service implementation for instructor discount usage operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorDiscountUsageServiceImp implements InstructorDiscountUsageService {

    private final InstructorDiscountUsageRepository instructorDiscountUsageRepository;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<InstructorDiscountUsageResponseDto>>> getDiscountUsages(
            Pageable pageable) {

        String instructorId = SecurityUtil.getCurrentUserId();
        log.info("Getting discount usages for instructor: {}, page: {}, size: {}",
                instructorId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<DiscountUsage> discountUsagePage = instructorDiscountUsageRepository
                    .findByCourseInstructorId(instructorId, pageable);

            Page<InstructorDiscountUsageResponseDto> responsePage = discountUsagePage
                    .map(InstructorDiscountUsageResponseDto::fromEntity);

            PaginatedResponse<InstructorDiscountUsageResponseDto> paginatedResponse = PaginatedResponse
                    .<InstructorDiscountUsageResponseDto>builder()
                    .content(responsePage.getContent())
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(responsePage.getNumber())
                            .size(responsePage.getSize())
                            .totalElements(responsePage.getTotalElements())
                            .totalPages(responsePage.getTotalPages())
                            .first(responsePage.isFirst())
                            .last(responsePage.isLast())
                            .build())
                    .build();

            log.info("Successfully retrieved {} discount usages for instructor: {}",
                    responsePage.getTotalElements(), instructorId);

            return ApiResponseUtil.success(paginatedResponse,
                    "Instructor discount usages retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discount usages for instructor: {}", instructorId, e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discount usages");
        }
    }
}
