package project.ktc.springboot_app.instructor_application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.exception.DocumentUploadException;
import project.ktc.springboot_app.common.exception.IneligibleApplicationException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.instructor_application.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.instructor_application.dto.AdminApplicationDetailDto;
import project.ktc.springboot_app.instructor_application.dto.AdminInstructorApplicationResponseDto;
import project.ktc.springboot_app.instructor_application.dto.AdminReviewApplicationRequestDto;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;
import project.ktc.springboot_app.instructor_application.interfaces.InstructorApplicationService;
import project.ktc.springboot_app.instructor_application.mapper.InstructorApplicationsMapper;
import project.ktc.springboot_app.instructor_application.repositories.InstructorApplicationRepository;
import project.ktc.springboot_app.upload.exception.InvalidDocumentFormatException;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.services.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation for instructor application operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorApplicationServiceImp implements InstructorApplicationService {

    private final InstructorApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final ObjectMapper objectMapper;
    private final InstructorApplicationsMapper instructorApplicationMapper;

    // Constants for business rules
    private static final int RESUBMISSION_DAYS_LIMIT = 3;
    private static final int MAX_REJECTION_COUNT = 2;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<DocumentUploadResponseDto>> uploadDocuments(
            MultipartFile certificate,
            String portfolio,
            MultipartFile cv,
            MultipartFile other) {

        log.info("Starting document upload for instructor application");

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Validate user eligibility
            validateUserEligibility(currentUserId);

            // Validate portfolio URL format (basic validation)
            validatePortfolioUrl(portfolio);

            // Validate required files
            validateRequiredFiles(certificate, cv);

            // Upload documents to Cloudinary
            Map<String, String> documentUrls = uploadDocumentsToCloudinary(certificate, cv, other);

            // Add portfolio URL to documents
            documentUrls.put("portfolio", portfolio);

            // Save or update instructor application
            saveInstructorApplication(currentUserId, documentUrls);

            // Build response
            DocumentUploadResponseDto response = DocumentUploadResponseDto.builder()
                    .userId(currentUserId)
                    .documents(documentUrls)
                    .build();

            log.info("Successfully uploaded documents for user: {}", currentUserId);
            return ApiResponseUtil.success(response, "Documents uploaded successfully");

        } catch (IneligibleApplicationException | InvalidDocumentFormatException e) {
            log.warn("Application validation failed: {}", e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (DocumentUploadException e) {
            log.error("Document upload failed: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during document upload: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to upload documents: " + e.getMessage());
        }
    }

    /**
     * Validate if user is eligible to submit instructor application
     */
    private void validateUserEligibility(String userId) {
        // Check if user has pending application
        if (applicationRepository.hasPendingApplication(userId)) {
            log.warn("User {} attempted to submit application while having a pending application", userId);
            throw new IneligibleApplicationException(
                    "You already have a pending application. Please wait for it to be reviewed before submitting a new one.");
        }

        // Check rejection count and resubmission eligibility
        long rejectionCount = applicationRepository.countRejectedApplicationsByUserId(userId);

        if (rejectionCount >= MAX_REJECTION_COUNT) {
            throw new IneligibleApplicationException(
                    "You have reached the maximum number of rejections (" + MAX_REJECTION_COUNT
                            + "). You cannot submit a new application at this time.");
        }

        // If user has one rejection, check if within resubmission period
        if (rejectionCount == 1) {
            Optional<InstructorApplication> latestRejection = applicationRepository.findLatestRejectedByUserId(userId);

            if (latestRejection.isPresent()) {
                LocalDateTime rejectionDate = latestRejection.get().getReviewedAt();
                long daysSinceRejection = ChronoUnit.DAYS.between(rejectionDate.toLocalDate(),
                        LocalDateTime.now().toLocalDate());

                if (daysSinceRejection > RESUBMISSION_DAYS_LIMIT) {
                    log.warn("User {} attempted to resubmit after expiration of resubmission period", userId);
                    throw new IneligibleApplicationException(
                            "You can resubmit your application after " + RESUBMISSION_DAYS_LIMIT
                                    + " days from your last rejection.");
                }
            }
        }

    }

    /**
     * Validate portfolio URL format
     */
    private void validatePortfolioUrl(String portfolio) {
        if (portfolio == null || portfolio.trim().isEmpty()) {
            throw new InvalidDocumentFormatException("Portfolio URL is required");
        }

        // Basic URL validation - allows various portfolio platforms
        String trimmedUrl = portfolio.trim().toLowerCase();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("www.")) {
            throw new InvalidDocumentFormatException(
                    "Portfolio must be a valid URL starting with http://, https://, or www.");
        }
    }

    /**
     * Validate required files are provided
     */
    private void validateRequiredFiles(MultipartFile certificate, MultipartFile cv) {
        if (certificate == null || certificate.isEmpty()) {
            throw new InvalidDocumentFormatException("Certificate file is required");
        }

        if (cv == null || cv.isEmpty()) {
            throw new InvalidDocumentFormatException("CV/Resume file is required");
        }
    }

    /**
     * Upload documents to Cloudinary and return URLs
     */
    private Map<String, String> uploadDocumentsToCloudinary(
            MultipartFile certificate, MultipartFile cv, MultipartFile other) {

        Map<String, String> documentUrls = new HashMap<>();

        try {
            // Upload certificate
            fileValidationService.validateDocumentFile(certificate);
            project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto certificateResult = cloudinaryService
                    .uploadDocument(certificate);
            documentUrls.put("certificate", certificateResult.getUrl());
            log.info("Certificate uploaded successfully: {}", certificateResult.getUrl());

            // Upload CV
            fileValidationService.validateDocumentFile(cv);
            project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto cvResult = cloudinaryService
                    .uploadDocument(cv);
            documentUrls.put("cv", cvResult.getUrl());
            log.info("CV uploaded successfully: {}", cvResult.getUrl());

            // Upload optional other document
            if (other != null && !other.isEmpty()) {
                fileValidationService.validateDocumentFile(other);
                project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto otherResult = cloudinaryService
                        .uploadDocument(other);
                documentUrls.put("other", otherResult.getUrl());
                log.info("Additional document uploaded successfully: {}", otherResult.getUrl());
            }

            return documentUrls;

        } catch (Exception e) {
            log.error("Failed to upload documents to Cloudinary: {}", e.getMessage(), e);
            throw new DocumentUploadException("Failed to upload documents to cloud storage", e);
        }
    }

    /**
     * Save or update instructor application with documents
     */
    private void saveInstructorApplication(String userId, Map<String, String> documentUrls) {
        try {
            // Convert documents map to JSON string
            String documentsJson = objectMapper.writeValueAsString(documentUrls);

            // Check if user already has an application (for resubmission cases)
            Optional<InstructorApplication> existingApplication = applicationRepository.findByUserId(userId);

            InstructorApplication application;
            if (existingApplication.isPresent()) {
                // Update existing application for resubmission
                application = existingApplication.get();
                application.setDocuments(documentsJson);
                application.setStatus(InstructorApplication.ApplicationStatus.PENDING);
                application.setSubmittedAt(LocalDateTime.now());
                application.setReviewedAt(null);
                application.setReviewedBy(null);
                application.setRejectionReason(null);
                log.info("Updating existing application for user: {}", userId);
            } else {
                // Create new application
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                application = new InstructorApplication();
                application.setUser(user);
                application.setDocuments(documentsJson);
                application.setStatus(InstructorApplication.ApplicationStatus.PENDING);
                application.setSubmittedAt(LocalDateTime.now());
                log.info("Creating new application for user: {}", userId);
            }

            applicationRepository.save(application);
            log.info("Instructor application saved successfully for user: {}", userId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize documents to JSON: {}", e.getMessage(), e);
            throw new DocumentUploadException("Failed to process document information", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<List<AdminInstructorApplicationResponseDto>>> getAllApplicationAdmin() {
        List<InstructorApplication> applications = applicationRepository.findAll();

        List<AdminInstructorApplicationResponseDto> responseList = instructorApplicationMapper
                .toAdminResponseDtoList(applications);

        if (responseList.isEmpty()) {
            log.warn("No instructor applications found");
            return ApiResponseUtil.success(responseList, "No instructor applications found");
        }

        return ApiResponseUtil.success(responseList, "Fetched all instructor applications successfully");
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<AdminApplicationDetailDto>> getApplicationByIdAdmin(String applicationId) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Check if application exists
            InstructorApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

            log.debug("Found application with ID: {}, Status: {}", application.getId(), application.getStatus());

            // Map to DTO
            AdminApplicationDetailDto responseDto = instructorApplicationMapper.toAdminDetailResponseDto(application);

            return ApiResponseUtil.success(responseDto,
                    "Fetched application detail successfully with id: " + applicationId);

        } catch (RuntimeException e) {
            log.error("Application not found: {}", e.getMessage());
            return ApiResponseUtil.notFound("Application not found with ID: " + applicationId);

        } catch (Exception e) {
            log.error("Error retrieving application detail: {}", e.getMessage(), e);
            return ApiResponseUtil
                    .internalServerError("Failed to retrieve application detail. Try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> reviewApplication(String applicationId,
            AdminReviewApplicationRequestDto request) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Get current user ID
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                log.warn("Current user ID is null");
                return ApiResponseUtil.internalServerError("Cannot retrieve current user ID");
            }

            // Find application
            InstructorApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            // Check if already reviewed
            if (!application.getStatus().equals(InstructorApplication.ApplicationStatus.PENDING)) {
                log.warn("Application {} already reviewed with status: {}", applicationId, application.getStatus());
                return ApiResponseUtil
                        .badRequest("Application already reviewed with status: " + application.getStatus());
            }

            // Validate rejection reason if rejecting
            if (request.getAction() == ApplicationStatus.REJECTED &&
                    (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
                return ApiResponseUtil.badRequest("Rejection reason is required when rejecting application");
            }

            // Update application

            application.setStatus(request.getAction());
            application.setReviewedAt(LocalDateTime.now());

            User reviewer = userRepository.findById(currentUserId).orElse(null);
            application.setReviewedBy(reviewer);

            if (request.getAction() == ApplicationStatus.REJECTED) {
                application.setRejectionReason(request.getRejectionReason());
            } else {
                application.setRejectionReason(null); // Clear rejection reason if approving
            }

            applicationRepository.save(application);

            String message = request.getAction() == ApplicationStatus.APPROVED
                    ? "Application approved"
                    : "Application rejected";

            log.info("Application {} {} by user {}", applicationId, request.getAction().name().toLowerCase(),
                    currentUserId);

            return ApiResponseUtil.success(message);

        } catch (RuntimeException e) {
            log.error("Application not found: {}", e.getMessage());
            return ApiResponseUtil.notFound("Cannot find application with ID: " + applicationId);

        } catch (Exception e) {
            log.error("Error reviewing application: {}", e.getMessage(), e);
            return ApiResponseUtil
                    .internalServerError("An error occurred while processing the application. Please try again later.");
        }
    }

}
