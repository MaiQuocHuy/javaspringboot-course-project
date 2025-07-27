package project.ktc.springboot_app.instructor_application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.exception.DocumentUploadException;
import project.ktc.springboot_app.common.exception.IneligibleApplicationException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.instructor_application.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.instructor_application.interfaces.InstructorApplicationService;
import project.ktc.springboot_app.instructor_application.repositories.InstructorApplicationRepository;
import project.ktc.springboot_app.upload.exception.InvalidDocumentFormatException;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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
                application.setStatus("PENDING");
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
                application.setStatus("PENDING");
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
}
