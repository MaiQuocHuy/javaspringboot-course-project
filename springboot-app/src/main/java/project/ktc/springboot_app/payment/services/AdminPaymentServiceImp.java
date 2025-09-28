package project.ktc.springboot_app.payment.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.model.checkout.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.earning.repositories.InstructorEarningRepository;
import project.ktc.springboot_app.enrollment.services.EnrollmentServiceImp;
import project.ktc.springboot_app.log.mapper.PaymentLogMapper;
import project.ktc.springboot_app.log.services.SystemLogHelper;
import project.ktc.springboot_app.notification.utils.NotificationHelper;
import project.ktc.springboot_app.payment.dto.AdminPaymentResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentStatisticsResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaidOutResponseDto;
import project.ktc.springboot_app.payment.dto.AdminUpdatePaymentStatusResponseDto;
import project.ktc.springboot_app.payment.dto.AdminPaymentDetailResponseDto;
import project.ktc.springboot_app.payment.entity.Payment;
import project.ktc.springboot_app.payment.entity.Payment.PaymentStatus;
import project.ktc.springboot_app.payment.interfaces.AdminPaymentService;
import project.ktc.springboot_app.payment.repositories.AdminPaymentRepository;
import project.ktc.springboot_app.refund.entity.Refund;
import project.ktc.springboot_app.stripe.services.StripePaymentDetailsService;
import project.ktc.springboot_app.utils.ExtractPaymentDetailFromSessionId;
import project.ktc.springboot_app.utils.ExtractPaymentDetailFromSessionId.PaymentDetailDto;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.stripe.services.StripeWebhookService;
import project.ktc.springboot_app.discount.interfaces.AffiliatePayoutService;
import project.ktc.springboot_app.discount.repositories.DiscountUsageRepository;
import project.ktc.springboot_app.discount.entity.DiscountUsage;
import project.ktc.springboot_app.discount.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Implementation of AdminPaymentService for admin payment operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AdminPaymentServiceImp implements AdminPaymentService {

    private final AdminPaymentRepository adminPaymentRepository;
    private final StripePaymentDetailsService stripePaymentDetailsService;
    private final SystemLogHelper systemLogHelper;
    private final EnrollmentServiceImp enrollmentService;
    private final StripeWebhookService stripeWebhookService;
    private final InstructorEarningRepository instructorEarningRepository;
    private final AffiliatePayoutService affiliatePayoutService;
    private final DiscountUsageRepository discountUsageRepository;
    private final NotificationHelper notificationHelper;
    private final CacheInvalidationService cacheInvalidationService;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminPaymentResponseDto>>> getAllPayments(Pageable pageable) {
        try {
            log.info("Admin retrieving all payments with pagination: page={}, size={}",
                    pageable.getPageNumber(), pageable.getPageSize());

            Page<Payment> payments = adminPaymentRepository.findAllPayments(pageable);

            List<AdminPaymentResponseDto> paymentDtos = payments.getContent().stream()
                    .map(AdminPaymentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            PaginatedResponse<AdminPaymentResponseDto> paginatedResponse = PaginatedResponse
                    .<AdminPaymentResponseDto>builder()
                    .content(paymentDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(payments.getNumber())
                            .size(payments.getSize())
                            .totalElements(payments.getTotalElements())
                            .totalPages(payments.getTotalPages())
                            .first(payments.isFirst())
                            .last(payments.isLast())
                            .build())
                    .build();

            log.info("Retrieved {} payments for admin (page {} of {})",
                    paymentDtos.size(),
                    payments.getNumber() + 1,
                    payments.getTotalPages());

            return ApiResponseUtil.success(paginatedResponse, "Payments retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving all payments for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<AdminPaymentResponseDto>>> getAllPayments() {
        try {
            log.info("Admin retrieving all payments without pagination");

            List<Payment> payments = adminPaymentRepository.findAllPayments();

            List<AdminPaymentResponseDto> paymentDtos = payments.stream()
                    .map(AdminPaymentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            log.info("Retrieved {} payments for admin", paymentDtos.size());

            return ApiResponseUtil.success(paymentDtos, "Payments retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving all payments for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminPaymentResponseDto>>> getAllPayments(
            String search,
            project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
            String fromDate,
            String toDate,
            String paymentMethod,
            Pageable pageable) {
        try {
            log.info(
                    "Admin retrieving payments with filters - search: {}, status: {}, fromDate: {}, toDate: {}, paymentMethod: {}, page: {}, size: {}",
                    search, status, fromDate, toDate, paymentMethod, pageable.getPageNumber(), pageable.getPageSize());

            // Parse date strings to LocalDate
            java.time.LocalDate parsedFromDate = null;
            java.time.LocalDate parsedToDate = null;

            try {
                if (fromDate != null && !fromDate.trim().isEmpty()) {
                    parsedFromDate = java.time.LocalDate.parse(fromDate);
                }
                if (toDate != null && !toDate.trim().isEmpty()) {
                    parsedToDate = java.time.LocalDate.parse(toDate);
                }
            } catch (java.time.format.DateTimeParseException e) {
                log.warn("Invalid date format provided - fromDate: {}, toDate: {}", fromDate, toDate);
                return ApiResponseUtil.badRequest("Invalid date format. Please use yyyy-MM-dd format.");
            }

            // Validate date range
            if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                return ApiResponseUtil.badRequest("From date cannot be after to date.");
            }

            Page<Payment> payments = adminPaymentRepository.findAllPaymentsWithFilter(
                    search, status, paymentMethod, parsedFromDate, parsedToDate, pageable);

            List<AdminPaymentResponseDto> paymentDtos = payments.getContent().stream()
                    .map(AdminPaymentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            PaginatedResponse<AdminPaymentResponseDto> paginatedResponse = PaginatedResponse
                    .<AdminPaymentResponseDto>builder()
                    .content(paymentDtos)
                    .page(PaginatedResponse.PageInfo.builder()
                            .number(payments.getNumber())
                            .size(payments.getSize())
                            .totalElements(payments.getTotalElements())
                            .totalPages(payments.getTotalPages())
                            .first(payments.isFirst())
                            .last(payments.isLast())
                            .build())
                    .build();

            log.info("Retrieved {} payments out of {} total with filters applied",
                    paymentDtos.size(), payments.getTotalElements());

            return ApiResponseUtil.success(paginatedResponse, "Payments retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving payments with filters for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<AdminPaymentResponseDto>>> getAllPayments(
            String search,
            project.ktc.springboot_app.payment.entity.Payment.PaymentStatus status,
            String fromDate,
            String toDate,
            String paymentMethod) {
        try {
            log.info(
                    "Admin retrieving all payments without pagination with filters - search: {}, status: {}, fromDate: {}, toDate: {}, paymentMethod: {}",
                    search, status, fromDate, toDate, paymentMethod);

            // Parse date strings to LocalDate
            java.time.LocalDate parsedFromDate = null;
            java.time.LocalDate parsedToDate = null;

            try {
                if (fromDate != null && !fromDate.trim().isEmpty()) {
                    parsedFromDate = java.time.LocalDate.parse(fromDate);
                }
                if (toDate != null && !toDate.trim().isEmpty()) {
                    parsedToDate = java.time.LocalDate.parse(toDate);
                }
            } catch (java.time.format.DateTimeParseException e) {
                log.warn("Invalid date format provided - fromDate: {}, toDate: {}", fromDate, toDate);
                return ApiResponseUtil.badRequest("Invalid date format. Please use yyyy-MM-dd format.");
            }

            // Validate date range
            if (parsedFromDate != null && parsedToDate != null && parsedFromDate.isAfter(parsedToDate)) {
                return ApiResponseUtil.badRequest("From date cannot be after to date.");
            }

            List<Payment> payments = adminPaymentRepository.findAllPaymentsWithFilter(
                    search, status, paymentMethod, parsedFromDate, parsedToDate);

            List<AdminPaymentResponseDto> paymentDtos = payments.stream()
                    .map(AdminPaymentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            log.info("Retrieved {} payments with filters applied", paymentDtos.size());

            return ApiResponseUtil.success(paymentDtos, "Payments retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving all payments with filters for admin: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<AdminPaymentDetailResponseDto>> getPaymentDetail(String paymentId) {
        try {
            log.info("Admin retrieving payment detail for payment: {}", paymentId);

            Optional<Payment> paymentOpt = adminPaymentRepository.findPaymentByIdWithDetails(paymentId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found: {}", paymentId);
                return ApiResponseUtil.notFound("Payment not found");
            }

            Payment payment = paymentOpt.get();
            AdminPaymentDetailResponseDto paymentDetail;

            // Check if this is a Stripe payment and fetch additional details
            if (stripePaymentDetailsService.isStripePayment(payment.getPaymentMethod()) &&
                    payment.getSessionId() != null) {

                log.info("Fetching Stripe payment details for payment: {} with session: {}",
                        paymentId, payment.getSessionId());

                try {
                    // Fetch Stripe payment details
                    var stripeData = stripePaymentDetailsService.fetchPaymentDetails(payment.getSessionId());

                    // Convert to admin stripe data format
                    AdminPaymentDetailResponseDto.StripePaymentData adminStripeData = null;
                    if (stripeData != null) {
                        adminStripeData = AdminPaymentDetailResponseDto.StripePaymentData.builder()
                                .transactionId(stripeData.getTransactionId())
                                .receiptUrl(stripeData.getReceiptUrl())
                                .cardBrand(stripeData.getCardBrand())
                                .cardLast4(stripeData.getCardLast4())
                                .cardExpMonth(stripeData.getCardExpMonth())
                                .cardExpYear(stripeData.getCardExpYear())
                                .build();
                    }

                    // Create DTO with Stripe data
                    paymentDetail = AdminPaymentDetailResponseDto.fromEntityWithStripeData(payment, adminStripeData);

                    log.info("Successfully fetched Stripe payment details for payment: {}", paymentId);
                } catch (Exception stripeError) {
                    log.warn("Failed to fetch Stripe details for payment {}: {}. Using basic payment info.",
                            paymentId, stripeError.getMessage());
                    // Fallback to basic payment info if Stripe fetch fails
                    paymentDetail = AdminPaymentDetailResponseDto.fromEntity(payment);
                }
            } else {
                // Create basic DTO without Stripe data
                paymentDetail = AdminPaymentDetailResponseDto.fromEntity(payment);
                log.info("Created basic payment detail for non-Stripe payment: {}", paymentId);
            }

            log.info("Retrieved payment detail for payment: {}", paymentId);
            return ApiResponseUtil.success(paymentDetail, "Payment detail retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving payment detail for payment {}: {}", paymentId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payment detail. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<AdminUpdatePaymentStatusResponseDto>> updatePaymentStatus(String paymentId,
            String newStatus) {
        try {
            log.info("Admin updating payment status for payment: {} to status: {}", paymentId, newStatus);

            // Validate that the new status is allowed
            if (!Arrays.asList("COMPLETED", "FAILED").contains(newStatus)) {
                log.warn("Invalid status update attempt: {} for payment: {}", newStatus, paymentId);
                return ApiResponseUtil.badRequest("Invalid status. Only COMPLETED or FAILED are allowed.");
            }

            Optional<Payment> paymentOpt = adminPaymentRepository.findPaymentByIdWithDetails(paymentId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found for status update: {}", paymentId);
                return ApiResponseUtil.notFound("Payment not found");
            }

            Payment payment = paymentOpt.get();

            // Check if payment is currently PENDING
            if (payment.getStatus() != PaymentStatus.PENDING) {
                log.warn("Cannot update payment status. Current status is: {} for payment: {}",
                        payment.getStatus(), paymentId);
                return ApiResponseUtil.badRequest(
                        String.format("Cannot update payment status. Payment is currently %s", payment.getStatus()));
            }

            // Store old status for logging
            PaymentStatus oldStatus = payment.getStatus();

            // Update payment status
            PaymentStatus paymentStatus = PaymentStatus.valueOf(newStatus);
            payment.setStatus(paymentStatus);

            // Set paidAt timestamp if status is COMPLETED
            if (paymentStatus == PaymentStatus.COMPLETED) {
                payment.setPaidAt(LocalDateTime.now());

                // Create enrollment for user when payment is completed
                try {
                    enrollmentService.createEnrollmentFromWebhook(
                            payment.getUser().getId(),
                            payment.getCourse().getId(),
                            payment.getSessionId());
                    log.info("Enrollment created successfully for user {} in course {} from payment {}",
                            payment.getUser().getId(), payment.getCourse().getId(), paymentId);
                    Session session = stripeWebhookService.getSessionById(payment.getSessionId());
                    log.info("Session retrieved for payment {}: {}", paymentId, session);
                    stripeWebhookService.sendPaymentConfirmationEmail(session,
                            payment.getCourse().getId(),
                            payment.getUser().getId());
                } catch (Exception e) {
                    log.error("Failed to create enrollment for user {} in course {} from payment {}: {}",
                            payment.getUser().getId(), payment.getCourse().getId(), paymentId, e.getMessage());
                    // Continue with payment update even if enrollment creation fails
                }
            }

            Payment updatedPayment = adminPaymentRepository.save(payment);

            // Log the status update
            try {
                User currentUser = SecurityUtil.getCurrentUser();
                var oldPaymentLog = PaymentLogMapper.toLogDto(payment); // Use original payment data as "old" values
                var newPaymentLog = PaymentLogMapper.toLogDto(updatedPayment);
                systemLogHelper.logUpdate(currentUser, "PAYMENT", paymentId, oldPaymentLog, newPaymentLog);
                log.info("Payment status update logged for payment {} from {} to {}",
                        paymentId, oldStatus, newStatus);
            } catch (Exception logError) {
                log.error("Failed to log payment status update: {}", logError.getMessage());
                // Continue execution even if logging fails
            }

            AdminUpdatePaymentStatusResponseDto responseDto = AdminUpdatePaymentStatusResponseDto.builder()
                    .id(updatedPayment.getId())
                    .paymentMethod(updatedPayment.getPaymentMethod())
                    .status(updatedPayment.getStatus().name())
                    .updatedAt(LocalDateTime.now())
                    .build();

            if (paymentStatus == PaymentStatus.COMPLETED) {
                String userName = payment.getUser().getName();
                String userId = payment.getUser().getId();
                String courseTitle = payment.getCourse().getTitle();
                String courseId = payment.getCourse().getId();

                // Create admin notification
                notificationHelper.createAdminStudentPaymentNotification(payment.getId(), userName,
                        courseTitle, payment.getAmount());

                // Create student payment success notification
                try {
                    String courseUrl = "/dashboard/learning/" + courseId;
                    notificationHelper.createPaymentSuccessNotification(
                            userId,
                            payment.getId(),
                            courseTitle,
                            courseUrl,
                            courseId)
                            .thenAccept(notification -> log.info(
                                    "✅ Payment success notification created for student {} ({}): {}",
                                    userName, userId, notification.getId()))
                            .exceptionally(ex -> {
                                log.error("❌ Failed to create payment success notification for student {}: {}",
                                        userId, ex.getMessage(), ex);
                                return null;
                            });

                    log.info(
                            "💰 Admin updated payment {} to COMPLETED for student {} - payment success notification created",
                            payment.getId(), userName);

                } catch (Exception studentNotificationError) {
                    log.error("❌ Failed to create student payment success notification: {}",
                            studentNotificationError.getMessage(), studentNotificationError);
                    // Continue execution even if student notification fails
                }
            }

            log.info("Successfully updated payment status for payment: {} from {} to {}",
                    paymentId, oldStatus, newStatus);

            return ApiResponseUtil.success(responseDto,
                    String.format("Payment status updated successfully from %s to %s", oldStatus, newStatus));

        } catch (Exception e) {
            log.error("Error updating payment status for payment {}: {}", paymentId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update payment status. Please try again later.");
        }

    }

    @Override
    public ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> getPaymentsByUserId(String userId,
            Pageable pageable) {
        try {
            log.info("Admin retrieving payments for user: {} with pagination: page={}, size={}",
                    userId, pageable.getPageNumber(), pageable.getPageSize());

            Page<Payment> payments = adminPaymentRepository.findPaymentsByUserIdForAdmin(userId, pageable);

            Page<AdminPaymentResponseDto> paymentDtos = payments.map(AdminPaymentResponseDto::fromEntity);

            log.info("Retrieved {} payments for user {} (page {} of {})",
                    paymentDtos.getNumberOfElements(), userId,
                    paymentDtos.getNumber() + 1,
                    paymentDtos.getTotalPages());

            return ApiResponseUtil.success(paymentDtos,
                    String.format("Payments for user %s retrieved successfully", userId));

        } catch (Exception e) {
            log.error("Error retrieving payments for user {} by admin: {}", userId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> getPaymentsByCourseId(String courseId,
            Pageable pageable) {
        try {
            log.info("Admin retrieving payments for course: {} with pagination: page={}, size={}",
                    courseId, pageable.getPageNumber(), pageable.getPageSize());

            Page<Payment> payments = adminPaymentRepository.findPaymentsByCourseIdForAdmin(courseId, pageable);

            Page<AdminPaymentResponseDto> paymentDtos = payments.map(AdminPaymentResponseDto::fromEntity);

            log.info("Retrieved {} payments for course {} (page {} of {})",
                    paymentDtos.getNumberOfElements(), courseId,
                    paymentDtos.getNumber() + 1,
                    paymentDtos.getTotalPages());

            return ApiResponseUtil.success(paymentDtos,
                    String.format("Payments for course %s retrieved successfully", courseId));

        } catch (Exception e) {
            log.error("Error retrieving payments for course {} by admin: {}", courseId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve payments. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Page<AdminPaymentResponseDto>>> searchPayments(String searchTerm,
            Pageable pageable) {
        try {
            log.info("Admin searching payments with term: '{}' with pagination: page={}, size={}",
                    searchTerm, pageable.getPageNumber(), pageable.getPageSize());

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Empty search term provided for payment search");
                return ApiResponseUtil.badRequest("Search term cannot be empty");
            }

            Page<Payment> payments = adminPaymentRepository.searchPayments(searchTerm.trim(), pageable);

            Page<AdminPaymentResponseDto> paymentDtos = payments.map(AdminPaymentResponseDto::fromEntity);

            log.info("Found {} payments matching search term '{}' (page {} of {})",
                    paymentDtos.getNumberOfElements(), searchTerm,
                    paymentDtos.getNumber() + 1,
                    paymentDtos.getTotalPages());

            return ApiResponseUtil.success(paymentDtos,
                    String.format("Search results for '%s' retrieved successfully", searchTerm));

        } catch (Exception e) {
            log.error("Error searching payments with term '{}' by admin: {}", searchTerm, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to search payments. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<AdminPaidOutResponseDto>> paidOutPayment(String paymentId) {
        try {
            log.info("Admin attempting to paid out payment: {}", paymentId);

            // 1. Find payment by ID
            Optional<Payment> paymentOpt = adminPaymentRepository.findPaymentByIdWithDetails(paymentId);
            if (paymentOpt.isEmpty()) {
                log.warn("Payment not found with ID: {}", paymentId);
                return ApiResponseUtil.notFound("Payment not found");
            }

            Payment payment = paymentOpt.get();

            // 2. Check if payment status is COMPLETED
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                log.warn("Payment {} is not in COMPLETED status. Current status: {}", paymentId, payment.getStatus());
                return ApiResponseUtil.badRequest("Payment must be in COMPLETED status to be paid out");
            }

            // 3. Check if payment is already paid out
            if (payment.getPaidOutAt() != null) {
                log.warn("Payment {} has already been paid out at {}", paymentId, payment.getPaidOutAt());
                return ApiResponseUtil.badRequest("Payment has already been paid out");
            }

            // 4. Check if 3-day waiting period has passed since payment completion
            LocalDateTime paymentCompletedAt = payment.getUpdatedAt(); // When status changed to COMPLETED
            LocalDateTime now = LocalDateTime.now();
            Duration timeSinceCompletion = Duration.between(paymentCompletedAt, now);

            if (timeSinceCompletion.toDays() < 3) {
                long hoursRemaining = 72 - timeSinceCompletion.toHours();
                log.warn("Payment {} is within 3-day waiting period. {} hours remaining",
                        paymentId, hoursRemaining);
                return ApiResponseUtil.badRequest(
                        String.format("Payment must wait 3 days before paid out. %d hours remaining",
                                hoursRemaining));
            }

            // 5. Check refund status - failed refunds allowed
            if (payment.getRefunds() != null && !payment.getRefunds().isEmpty()) {
                boolean hasPendingRefund = payment.getRefunds().stream()
                        .anyMatch(refund -> refund.getStatus() == Refund.RefundStatus.PENDING);
                boolean hasCompletedRefund = payment.getRefunds().stream()
                        .anyMatch(refund -> refund.getStatus() == Refund.RefundStatus.COMPLETED);
                if (hasPendingRefund) {
                    log.warn("Payment {} has pending refund(s). Cannot paid out", paymentId);
                    return ApiResponseUtil.badRequest("Cannot paid out payment with pending refunds");
                }
                if (hasCompletedRefund) {
                    log.warn("Payment {} has completed refund(s). Cannot paid out", paymentId);
                    return ApiResponseUtil.badRequest("Cannot paid out payment with completed refunds");
                }
            }

            // 6. Check if instructor earning already exists
            Optional<InstructorEarning> existingEarning = instructorEarningRepository.findByPaymentId(paymentId);
            if (existingEarning.isPresent()) {
                log.warn("Instructor earning already exists for payment {}: {}", paymentId,
                        existingEarning.get().getId());
                return ApiResponseUtil.badRequest("Instructor earning already exists for this payment");
            }

            // 7. Calculate instructor earning using original price from Stripe session
            BigDecimal platformFeeRate = new BigDecimal("0.30"); // 30% platform fee
            BigDecimal instructorRate = BigDecimal.ONE.subtract(platformFeeRate); // 70% to instructor

            BigDecimal baseAmountForCalculation = payment.getAmount(); // Default to payment amount

            // Try to get original price from Stripe session if available
            if (payment.getSessionId() != null && !payment.getSessionId().trim().isEmpty()) {
                try {
                    log.info("Extracting original price from Stripe session for payment: {}", paymentId);
                    PaymentDetailDto paymentDetails = ExtractPaymentDetailFromSessionId
                            .extractPaymentDetails(payment.getSessionId());

                    if (paymentDetails.getOriginalPrice() != null) {
                        baseAmountForCalculation = paymentDetails.getOriginalPrice();
                        log.info(
                                "Using original price {} from Stripe session instead of discounted amount {} for instructor earning calculation",
                                baseAmountForCalculation, payment.getAmount());
                    } else {
                        log.warn("Original price not found in Stripe session metadata, using payment amount: {}",
                                payment.getAmount());
                    }
                } catch (Exception e) {
                    log.error(
                            "Failed to extract original price from Stripe session {}: {}. Using payment amount instead.",
                            payment.getSessionId(), e.getMessage(), e);
                }
            } else {
                log.info("No session ID found for payment {}, using payment amount for calculation", paymentId);
            }

            BigDecimal instructorEarningAmount = baseAmountForCalculation.multiply(instructorRate);

            // 8. Create instructor earning record
            InstructorEarning instructorEarning = new InstructorEarning();
            instructorEarning.setInstructor(payment.getCourse().getInstructor());
            instructorEarning.setPayment(payment);
            instructorEarning.setCourse(payment.getCourse());
            instructorEarning.setAmount(instructorEarningAmount);
            instructorEarning.setStatus(InstructorEarning.EarningStatus.AVAILABLE);
            instructorEarning.setPaidAt(null); // Will be set when actually paid to instructor

            InstructorEarning savedEarning = instructorEarningRepository.save(instructorEarning);

            // 9. Update payment paid out timestamp
            payment.setPaidOutAt(now);
            Payment updatedPayment = adminPaymentRepository.save(payment);

            // 10. Build response
            AdminPaidOutResponseDto response = AdminPaidOutResponseDto.builder()
                    .paymentId(updatedPayment.getId())
                    .courseId(updatedPayment.getCourse().getId())
                    .courseTitle(updatedPayment.getCourse().getTitle())
                    .instructorId(updatedPayment.getCourse().getInstructor().getId())
                    .instructorName(updatedPayment.getCourse().getInstructor().getName())
                    .amount(updatedPayment.getAmount())
                    .instructorEarning(instructorEarningAmount)
                    .paidOutAt(updatedPayment.getPaidOutAt())
                    .earningId(savedEarning.getId())
                    .message(String.format(
                            "Payment successfully paid out to instructor. Instructor earning calculated from %s amount: %s",
                            baseAmountForCalculation.equals(payment.getAmount()) ? "discounted" : "original",
                            baseAmountForCalculation))
                    .build();

            // Create affiliate payout for referral discounts when payment is paid out
            createAffiliatePayoutForReferralDiscount(payment);

            // Invalidate relevant caches
            cacheInvalidationService
                    .invalidateInstructorStatisticsOnPayment(payment.getCourse().getInstructor().getId());

            log.info(
                    "Payment {} successfully paid out. Instructor earning created: {}. Base amount used for calculation: {} ({})",
                    paymentId, savedEarning.getId(), baseAmountForCalculation,
                    baseAmountForCalculation.equals(payment.getAmount()) ? "discounted amount"
                            : "original price from Stripe");
            return ApiResponseUtil.success(response, "Payment paid out successfully");

        } catch (Exception e) {
            log.error("Error processing paid out for payment {}: {}", paymentId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to process paid out. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<AdminPaymentStatisticsResponseDto>> getPaymentStatistics() {
        try {
            log.info("Admin retrieving payment statistics");

            // Get payment counts
            Long totalPayments = adminPaymentRepository.countAllPayments();
            Long pendingPayments = adminPaymentRepository.countPaymentsByStatus(PaymentStatus.PENDING);
            Long completedPayments = adminPaymentRepository.countPaymentsByStatus(PaymentStatus.COMPLETED);
            Long failedPayments = adminPaymentRepository.countPaymentsByStatus(PaymentStatus.FAILED);
            Long refundedPayments = adminPaymentRepository.countPaymentsByStatus(PaymentStatus.REFUNDED);

            AdminPaymentStatisticsResponseDto paymentStatistics = AdminPaymentStatisticsResponseDto
                    .builder()
                    .total(totalPayments)
                    .pending(pendingPayments)
                    .completed(completedPayments)
                    .failed(failedPayments)
                    .refunded(refundedPayments)
                    .build();

            log.info("Payment statistics: Total={}, Pending={}, Completed={}, Failed={}, Refunded={}",
                    totalPayments, pendingPayments, completedPayments, failedPayments, refundedPayments);

            return ApiResponseUtil.success(paymentStatistics, "Payment statistics retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving payment statistics: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve statistics. Please try again later.");
        }
    }

    /**
     * Creates affiliate payout for referral discounts when payment is paid out
     * This ensures commission is only paid when payment is actually processed
     */
    private void createAffiliatePayoutForReferralDiscount(Payment payment) {
        try {
            log.info("🔍 Checking for affiliate payout eligibility for payment: {}", payment.getId());

            // Find discount usage records for this payment
            List<DiscountUsage> discountUsages = discountUsageRepository
                    .findByUserIdAndCourseId(payment.getUser().getId(), payment.getCourse().getId());

            if (discountUsages.isEmpty()) {
                log.debug("No discount usage found for payment: {}", payment.getId());
                return;
            }

            // Process each discount usage that might be eligible for affiliate payout
            for (DiscountUsage usage : discountUsages) {
                if (usage.getDiscount().getType() == DiscountType.REFERRAL &&
                        usage.getReferredByUser() != null) {

                    log.info("🎯 Creating affiliate payout for referral discount: {} used in payment: {}",
                            usage.getDiscount().getCode(), payment.getId());

                    // Create affiliate payout with PAID status (since payment is being paid out)
                    affiliatePayoutService.createPayoutAsync(usage, payment.getAmount(), payment.getId())
                            .thenAccept(payout -> {
                                log.info(
                                        "✅ Affiliate payout created successfully: {} for referrer: {} with commission: ${}",
                                        payout.getId(), usage.getReferredByUser().getId(),
                                        payout.getCommissionAmount());
                            })
                            .exceptionally(throwable -> {
                                log.error("❌ Failed to create affiliate payout for discount usage: {} - {}",
                                        usage.getId(), throwable.getMessage());
                                return null;
                            });
                }
            }

        } catch (Exception e) {
            log.error("❌ Error in affiliate payout creation for payment: {} - {}",
                    payment.getId(), e.getMessage(), e);
            // Don't fail the payment payout process for affiliate payout errors
        }
    }
}
