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
import project.ktc.springboot_app.discount.dto.StudentDiscountUsageResponseDto;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.interfaces.StudentDiscountUsageService;
import project.ktc.springboot_app.discount.repositories.StudentDiscountUsageRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

/**
 * Service implementation for student discount usage operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentDiscountUsageServiceImp implements StudentDiscountUsageService {

    private final StudentDiscountUsageRepository studentDiscountUsageRepository;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<StudentDiscountUsageResponseDto>>> getDiscountUsages(
            Pageable pageable) {

        String studentId = SecurityUtil.getCurrentUserId();
        log.info("Getting discount usages for student: {}, page: {}, size: {}",
                studentId, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<DiscountUsage> discountUsagePage = studentDiscountUsageRepository.findByReferredByUserId(studentId,
                    pageable);

            Page<StudentDiscountUsageResponseDto> responsePage = discountUsagePage.map(this::mapToResponseDto);

            PaginatedResponse<StudentDiscountUsageResponseDto> paginatedResponse = PaginatedResponse
                    .<StudentDiscountUsageResponseDto>builder()
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

            log.info("Successfully retrieved {} discount usages for student: {}",
                    responsePage.getTotalElements(), studentId);

            return ApiResponseUtil.success(paginatedResponse,
                    "Student discount usages retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving discount usages for student: {}", studentId, e);
            return ApiResponseUtil.internalServerError("Failed to retrieve discount usages");
        }
    }

    private StudentDiscountUsageResponseDto mapToResponseDto(DiscountUsage discountUsage) {
        return StudentDiscountUsageResponseDto.builder()
                .id(discountUsage.getId())
                .discount(StudentDiscountUsageResponseDto.DiscountInfo.builder()
                        .id(discountUsage.getDiscount().getId())
                        .code(discountUsage.getDiscount().getCode())
                        .description(discountUsage.getDiscount().getDescription())
                        .build())
                .user(StudentDiscountUsageResponseDto.UserInfo.builder()
                        .id(discountUsage.getUser().getId())
                        .name(discountUsage.getUser().getName())
                        .email(discountUsage.getUser().getEmail())
                        .thumbnailUrl(discountUsage.getUser().getThumbnailUrl())
                        .build())
                .course(StudentDiscountUsageResponseDto.CourseInfo.builder()
                        .id(discountUsage.getCourse().getId())
                        .title(discountUsage.getCourse().getTitle())
                        .instructor(StudentDiscountUsageResponseDto.UserInfo.builder()
                                .id(discountUsage.getCourse().getInstructor().getId())
                                .name(discountUsage.getCourse().getInstructor().getName())
                                .email(discountUsage.getCourse().getInstructor().getEmail())
                                .thumbnailUrl(discountUsage.getCourse().getInstructor().getThumbnailUrl())
                                .build())
                        .price(discountUsage.getCourse().getPrice())
                        .build())
                .usedAt(discountUsage.getUsedAt())
                .discountPercent(discountUsage.getDiscountPercent())
                .discountAmount(discountUsage.getDiscountAmount())
                .build();
    }
}
