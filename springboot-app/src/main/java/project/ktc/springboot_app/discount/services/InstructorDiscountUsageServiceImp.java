package project.ktc.springboot_app.discount.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
import project.ktc.springboot_app.discount.enums.DiscountType;
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

        @Override
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorDiscountUsageResponseDto>> getDiscountUsageById(
                        String discountUsageId) {

                String instructorId = SecurityUtil.getCurrentUserId();
                log.info("Getting discount usage {} for instructor: {}", discountUsageId, instructorId);

                try {
                        DiscountUsage discountUsage = instructorDiscountUsageRepository
                                        .findByIdAndCourseInstructorId(discountUsageId, instructorId);

                        if (discountUsage == null) {
                                log.warn("Discount usage {} not found for instructor: {}", discountUsageId,
                                                instructorId);
                                return ApiResponseUtil.notFound(
                                                "Discount usage not found or you don't have permission to view it");
                        }

                        InstructorDiscountUsageResponseDto responseDto = InstructorDiscountUsageResponseDto
                                        .fromEntity(discountUsage);

                        return ApiResponseUtil.success(responseDto, "Discount usage retrieved successfully");

                } catch (Exception e) {
                        log.error("Error retrieving discount usage {} for instructor: {}", discountUsageId,
                                        instructorId, e);
                        return ApiResponseUtil.internalServerError("Failed to retrieve discount usage");
                }
        }

        @Override
        public ResponseEntity<ApiResponse<PaginatedResponse<InstructorDiscountUsageResponseDto>>> getDiscountUsages(
                        String search, DiscountType type, String fromDate, String toDate, Pageable pageable) {

                String instructorId = SecurityUtil.getCurrentUserId();
                log.info("Getting discount usages for instructor: {} with filters - search: {}, type: {}, fromDate: {}, toDate: {}, page: {}, size: {}",
                                instructorId, search, type, fromDate, toDate, pageable.getPageNumber(),
                                pageable.getPageSize());

                try {
                        // Validate and parse dates
                        LocalDate fromLocalDate = null;
                        LocalDate toLocalDate = null;

                        if (fromDate != null && !fromDate.trim().isEmpty()) {
                                try {
                                        fromLocalDate = LocalDate.parse(fromDate);
                                } catch (DateTimeParseException e) {
                                        log.warn("Invalid fromDate format: {}", fromDate);
                                        return ApiResponseUtil.badRequest("Invalid fromDate format. Use YYYY-MM-DD");
                                }
                        }

                        if (toDate != null && !toDate.trim().isEmpty()) {
                                try {
                                        toLocalDate = LocalDate.parse(toDate);
                                } catch (DateTimeParseException e) {
                                        log.warn("Invalid toDate format: {}", toDate);
                                        return ApiResponseUtil.badRequest("Invalid toDate format. Use YYYY-MM-DD");
                                }
                        }

                        // Validate date range
                        if (fromLocalDate != null && toLocalDate != null && fromLocalDate.isAfter(toLocalDate)) {
                                log.warn("Invalid date range: fromDate {} is after toDate {}", fromDate, toDate);
                                return ApiResponseUtil.badRequest("fromDate cannot be after toDate");
                        }

                        Page<DiscountUsage> discountUsagePage = instructorDiscountUsageRepository
                                        .findByCourseInstructorIdWithFilter(instructorId, search, type, fromLocalDate,
                                                        toLocalDate, pageable);

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

                        log.info("Successfully retrieved {} filtered discount usages for instructor: {}",
                                        responsePage.getTotalElements(), instructorId);

                        return ApiResponseUtil.success(paginatedResponse,
                                        "Instructor discount usages retrieved successfully");

                } catch (Exception e) {
                        log.error("Error retrieving filtered discount usages for instructor: {}", instructorId, e);
                        return ApiResponseUtil.internalServerError("Failed to retrieve discount usages");
                }
        }
}
