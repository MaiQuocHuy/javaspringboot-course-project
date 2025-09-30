package project.ktc.springboot_app.refund.services;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.refund.dto.AdminRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatisticsResponseDto;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.refund.interfaces.AdminRefundService;
import project.ktc.springboot_app.refund.repositories.AdminRefundRepository;
import project.ktc.springboot_app.refund.repositories.RefundRepository;
import project.ktc.springboot_app.stripe.services.StripePaymentDetailsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRefundServiceImp implements AdminRefundService {

  private final AdminRefundRepository adminRefundRepository;
  private final RefundRepository refundRepository;
  private final InstructorEarningRepository instructorEarningRepository;
  private final StripePaymentDetailsService stripePaymentDetailsService;
  private final EnrollmentRepository enrollmentRepository;

  @Override
  public ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(
      Pageable pageable) {
    try {
      log.info(
          "Admin retrieving all refunds with pagination: page={}, size={}",
          pageable.getPageNumber(),
          pageable.getPageSize());
      Page<Refund> refunds = adminRefundRepository.findAllRefunds(pageable);

      List<AdminRefundResponseDto> refundDtos =
          refunds.getContent().stream()
              .map(AdminRefundResponseDto::fromEntity)
              .collect(Collectors.toList());

      PaginatedResponse<AdminRefundResponseDto> paginatedResponse =
          PaginatedResponse.<AdminRefundResponseDto>builder()
              .content(refundDtos)
              .page(
                  PaginatedResponse.PageInfo.builder()
                      .number(refunds.getNumber())
                      .size(refunds.getSize())
                      .totalElements(refunds.getTotalElements())
                      .totalPages(refunds.getTotalPages())
                      .first(refunds.isFirst())
                      .last(refunds.isLast())
                      .build())
              .build();

      log.info(
          "Retrieved {} refunds for admin (page {} of {})",
          refundDtos.size(),
          refunds.getNumber(),
          refunds.getTotalPages());

      return ApiResponseUtil.success(paginatedResponse, "Refunds retrieved successfully!");

    } catch (Exception e) {
      log.error("Error retrieving all refunds for admin: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Error retrieving all refunds for admin");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds() {
    try {
      log.info("Admin retrieving all refunds without pagination");

      List<Refund> refunds = adminRefundRepository.findAllRefunds();

      List<AdminRefundResponseDto> refundDtos =
          refunds.stream().map(AdminRefundResponseDto::fromEntity).collect(Collectors.toList());

      log.info("Retrieved {} refunds for admin", refundDtos.size());

      return ApiResponseUtil.success(refundDtos, "Refunds retrieved successfully!");

    } catch (Exception e) {
      log.error("Error retrieving all refunds for admin: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Error retrieving all refunds for admin");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<AdminRefundDetailsResponseDto>> getRefundDetail(
      String refundId) {
    try {
      log.info("Admin retrieving refund details for refund: {}", refundId);
      Optional<Refund> refundOpt = adminRefundRepository.findRefundByIdWithDetails(refundId);
      if (refundOpt.isEmpty()) {
        log.warn("Refund not found with ID: {}", refundId);
        return ApiResponseUtil.notFound("Refund not found");
      }
      Refund refund = refundOpt.get();
      AdminRefundDetailsResponseDto refundDetail;
      // Check if this is a Stripe payment and fetch additional details
      if (stripePaymentDetailsService.isStripePayment(refund.getPayment().getPaymentMethod())
          && refund.getPayment().getSessionId() != null) {
        try {
          var stripeData =
              stripePaymentDetailsService.fetchPaymentDetails(refund.getPayment().getSessionId());
          AdminRefundDetailsResponseDto.StripePaymentData adminStripeData = null;
          if (stripeData != null) {
            adminStripeData =
                AdminRefundDetailsResponseDto.StripePaymentData.builder()
                    .transactionId(stripeData.getTransactionId())
                    .receiptUrl(stripeData.getReceiptUrl())
                    .cardBrand(stripeData.getCardBrand())
                    .cardLast4(stripeData.getCardLast4())
                    .cardExpMonth(stripeData.getCardExpMonth())
                    .cardExpYear(stripeData.getCardExpYear())
                    .build();
          }
          refundDetail =
              AdminRefundDetailsResponseDto.fromEntityWithStripeData(refund, adminStripeData);

        } catch (Exception e) {
          log.error("Error fetching Stripe payment details for refund: {}", refundId);
          refundDetail = AdminRefundDetailsResponseDto.fromEntity(refund);
        }
      } else {
        refundDetail = AdminRefundDetailsResponseDto.fromEntity(refund);
        log.info(
            "Refund {} is not a Stripe payment or has no session ID, using basic details",
            refundId);
      }
      log.info("Refund details retrieved successfully for refund ID: {}", refundId);
      return ApiResponseUtil.success(refundDetail, "Refund details retrieved successfully!");
    } catch (Exception e) {
      log.error("Error retrieving refund details for admin: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Error retrieving refund details for admin");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(
      String search,
      Refund.RefundStatus status,
      String fromDate,
      String toDate,
      Pageable pageable) {
    try {
      log.info(
          "Admin retrieving refunds with filters - search: {}, status: {}, fromDate: {}, toDate: {}, page: {}, size: {}",
          search,
          status,
          fromDate,
          toDate,
          pageable.getPageNumber(),
          pageable.getPageSize());

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

      Page<Refund> refunds =
          adminRefundRepository.findAllRefundsWithFilter(
              search, status, fromLocalDate, toLocalDate, pageable);

      List<AdminRefundResponseDto> refundDtos =
          refunds.getContent().stream()
              .map(AdminRefundResponseDto::fromEntity)
              .collect(Collectors.toList());

      PaginatedResponse<AdminRefundResponseDto> paginatedResponse =
          PaginatedResponse.<AdminRefundResponseDto>builder()
              .content(refundDtos)
              .page(
                  PaginatedResponse.PageInfo.builder()
                      .number(refunds.getNumber())
                      .size(refunds.getSize())
                      .totalElements(refunds.getTotalElements())
                      .totalPages(refunds.getTotalPages())
                      .first(refunds.isFirst())
                      .last(refunds.isLast())
                      .build())
              .build();

      log.info(
          "Retrieved {} filtered refunds for admin (page {} of {})",
          refundDtos.size(),
          refunds.getNumber(),
          refunds.getTotalPages());

      return ApiResponseUtil.success(paginatedResponse, "Refunds retrieved successfully!");

    } catch (Exception e) {
      log.error("Error retrieving filtered refunds for admin: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Error retrieving filtered refunds for admin");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds(
      String search, Refund.RefundStatus status, String fromDate, String toDate) {
    try {
      log.info(
          "Admin retrieving all refunds with filters without pagination - search: {}, status: {}, fromDate: {}, toDate: {}",
          search,
          status,
          fromDate,
          toDate);

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

      List<Refund> refunds =
          adminRefundRepository.findAllRefundsWithFilter(
              search, status, fromLocalDate, toLocalDate);

      List<AdminRefundResponseDto> refundDtos =
          refunds.stream().map(AdminRefundResponseDto::fromEntity).collect(Collectors.toList());

      log.info("Retrieved {} filtered refunds for admin", refundDtos.size());

      return ApiResponseUtil.success(refundDtos, "Refunds retrieved successfully!");

    } catch (Exception e) {
      log.error("Error retrieving filtered refunds for admin: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError("Error retrieving filtered refunds for admin");
    }
  }

  @Override
  public ResponseEntity<ApiResponse<AdminRefundStatisticsResponseDto>> getRefundStatistics() {
    try {
      log.info("Admin retrieving refund statistics");

      // Fetch refund counts by status
      Long totalRefunds = adminRefundRepository.countAllRefunds();
      Long completedRefunds =
          adminRefundRepository.countRefundsByStatus(Refund.RefundStatus.COMPLETED);
      Long pendingRefunds = adminRefundRepository.countRefundsByStatus(Refund.RefundStatus.PENDING);
      Long failedRefunds = adminRefundRepository.countRefundsByStatus(Refund.RefundStatus.FAILED);

      AdminRefundStatisticsResponseDto refundStatistics =
          AdminRefundStatisticsResponseDto.builder()
              .total(totalRefunds)
              .completed(completedRefunds)
              .pending(pendingRefunds)
              .failed(failedRefunds)
              .build();

      log.info("Retrieved refund statistics: {}", refundStatistics);
      return ApiResponseUtil.success(refundStatistics, "Refund statistics retrieved successfully");

    } catch (Exception e) {
      log.error("Error retrieving refund statistics: {}", e.getMessage(), e);
      return ApiResponseUtil.internalServerError(
          "Failed to retrieve refund statistics. Please try again later.");
    }
  }
}
