
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
import project.ktc.springboot_app.instructor_application.dto.InstructorApplicationDetailResponseDto;
import project.ktc.springboot_app.instructor_application.dto.AdminInstructorApplicationResponseDto;
import project.ktc.springboot_app.instructor_application.dto.AdminReviewApplicationRequestDto;
import project.ktc.springboot_app.instructor_application.dto.DeleteApplicationDto;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;
import project.ktc.springboot_app.instructor_application.interfaces.InstructorApplicationService;
import project.ktc.springboot_app.instructor_application.mapper.InstructorApplicationsMapper;
import project.ktc.springboot_app.instructor_application.repositories.InstructorApplicationRepository;
import project.ktc.springboot_app.notification.utils.NotificationHelper;
import project.ktc.springboot_app.upload.exception.InvalidDocumentFormatException;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.services.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final NotificationHelper notificationHelper;

    // Constants for business rules
    private static final int MAX_REJECTION_COUNT = 5;

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
        // Check if user has active application (PENDING or APPROVED)

        var statuses = applicationRepository.findActiveStatusesByUserId(userId);

        if (statuses.contains(ApplicationStatus.PENDING)) {
            throw new IneligibleApplicationException("You already have a pending application...");
        }
        if (statuses.contains(ApplicationStatus.APPROVED)) {
            throw new IneligibleApplicationException("You are already an approved instructor.");
        }

        // Check rejection count and resubmission eligibility
        long rejectionCount = applicationRepository.countRejectedApplicationsByUserId(userId);

        if (rejectionCount >= MAX_REJECTION_COUNT) {
            throw new IneligibleApplicationException(
                    "You have reached the maximum number of rejections (" + MAX_REJECTION_COUNT
                            + "). You cannot submit a new application at this time.");
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

            // Always create a new application for each submission
            // This ensures proper tracking of rejection attempts
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            InstructorApplication application = new InstructorApplication();
            application.setUser(user);
            application.setDocuments(documentsJson);
            application.setStatus(InstructorApplication.ApplicationStatus.PENDING);
            application.setSubmittedAt(LocalDateTime.now());

            applicationRepository.save(application);
            log.info("New instructor application created successfully for user: {}", userId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize documents to JSON: {}", e.getMessage(), e);
            throw new DocumentUploadException("Failed to process document information", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<InstructorApplicationDetailResponseDto>> getApplicationByUserId(String userId) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Check if application exists
            InstructorApplication application = applicationRepository.findFirstByUserIdOrderBySubmittedAtDesc(userId)
                    .orElseThrow(() -> new RuntimeException("Application not found for user id: " + userId));

            log.debug("Found application for User ID: {}, Status: {}", application.getUser().getId(),
                    application.getStatus());

            // Map to DTO
            InstructorApplicationDetailResponseDto responseDto = instructorApplicationMapper
                    .toApplicationDetailResponseDto(application);

            long submitAttemptRemain = MAX_REJECTION_COUNT
                    - applicationRepository.countRejectedApplicationsByUserId(userId);

            responseDto.setSubmitAttemptRemain(submitAttemptRemain);

            return ApiResponseUtil.success(responseDto,
                    "Fetched application detail successfully for user id: " + userId);

        } catch (RuntimeException e) {
            log.error("Application not found: {}", e.getMessage());
            return ApiResponseUtil.notFound("Application not found for User ID: " + userId);

        } catch (Exception e) {
            log.error("Error retrieving application detail: {}", e.getMessage(), e);
            return ApiResponseUtil
                    .internalServerError("Failed to retrieve application detail. Try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<List<AdminInstructorApplicationResponseDto>>> getAllApplicationAdmin() {
        List<InstructorApplication> applications = applicationRepository.findAllLatestPerUser();

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
    public ResponseEntity<ApiResponse<List<AdminInstructorApplicationResponseDto>>> getApplicationByUserIdAdmin(
            String userId) {
        List<InstructorApplication> applications = applicationRepository.findByUserIdAdmin(userId);

        List<AdminInstructorApplicationResponseDto> responseList = instructorApplicationMapper
                .toAdminDetailResponseDtoList(applications);

        if (responseList.isEmpty()) {
            log.warn("No instructor applications found");
            return ApiResponseUtil.success(responseList, "No instructor applications found");
        }

        return ApiResponseUtil.success(responseList,
                "Fetched applications successfully for user ID: " + userId);
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
                // Notify applicant about rejection
                notificationHelper.createStudentInstructorApplicationRejectedNotification(application.getUser().getId(),
                        application.getId(), request.getRejectionReason());
            } else {
                application.setRejectionReason(null); // Clear rejection reason if approving
                notificationHelper.createStudentInstructorApplicationApprovedNotification(application.getUser().getId(),
                        application.getId());
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

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteApplicationById(String applicationId) {
        try {
            // Check authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Find application
            InstructorApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            // Xóa documents trên Cloudinary trước khi xóa application
            if (application.getDocuments() != null && !application.getDocuments().isEmpty()) {
                try {
                    log.info("Deleting documents from Cloudinary for application: {}", applicationId);
                    deleteAllDocumentsFromCloudinary(application.getDocuments());
                } catch (Exception e) {
                    // Log lỗi nhưng vẫn tiếp tục xóa application
                    log.error("Error deleting documents from Cloudinary, continuing with application deletion: {}",
                            e.getMessage());
                }
            }

            // Xóa application từ database
            applicationRepository.delete(application);
            log.info("Application {} deleted by user {}", applicationId, authentication.getName());

            return ApiResponseUtil.success("Application deleted successfully");

        } catch (RuntimeException e) {
            log.error("Application not found: {}", e.getMessage());
            return ApiResponseUtil.notFound("Cannot find application with ID: " + applicationId);
        } catch (Exception e) {
            log.error("Error deleting application: {}", e.getMessage(), e);
            return ApiResponseUtil
                    .internalServerError("An error occurred while processing the application. Please try again later.");
        }
    }

    /**
     * Xóa tất cả documents từ Cloudinary dựa trên JSON string
     * 
     * @param documentsJson JSON string chứa URLs của documents
     */

    private void deleteAllDocumentsFromCloudinary(String documentsJson) {
        if (documentsJson == null || documentsJson.isEmpty()) {
            log.info("No documents to delete");
            return;
        }

        try {
            // Parse JSON thành object
            DeleteApplicationDto documents = objectMapper.readValue(
                    documentsJson,
                    DeleteApplicationDto.class);

            // Xóa từng document (trừ portfolio vì là GitHub link)
            deleteDocumentIfCloudinary(documents.getCv(), "CV");
            deleteDocumentIfCloudinary(documents.getOther(), "Other document");
            deleteDocumentIfCloudinary(documents.getCertificate(), "Certificate");

            // Portfolio thường là GitHub link nên không cần xóa
            if (documents.getPortfolio() != null &&
                    documents.getPortfolio().contains("cloudinary.com")) {
                deleteDocumentIfCloudinary(documents.getPortfolio(), "Portfolio");
            }

            log.info("Successfully processed document deletion from Cloudinary");

        } catch (Exception e) {
            log.error("Error parsing or deleting documents from JSON: {}", documentsJson, e);
            // Không throw exception để không ảnh hưởng việc xóa application
        }
    }

    /**
     * Xóa document nếu là Cloudinary URL
     */

    private void deleteDocumentIfCloudinary(String url, String documentType) {
        if (url == null || url.isEmpty()) {
            log.debug("No {} to delete", documentType);
            return;
        }

        if (!url.contains("cloudinary.com")) {
            log.debug("{} is not a Cloudinary URL: {}", documentType, url);
            return;
        }

        try {
            String publicId = extractPublicIdFromUrl(url);
            if (publicId != null) {
                boolean deleted = cloudinaryService.deleteDocument(publicId);
                if (deleted) {
                    log.info("Successfully deleted {} from Cloudinary: {}", documentType, publicId);
                } else {
                    log.warn("Failed to delete {} from Cloudinary: {}", documentType, publicId);
                }
            }
        } catch (Exception e) {
            log.error("Error deleting {} from Cloudinary: {}", documentType, url, e);
        }
    }

    private String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.contains("cloudinary.com")) {
            return null;
        }
        // Matches: /upload/v{version}/instructor-documents/{filename_with_extension}
        Pattern pattern = Pattern.compile("/upload/(?:v\\d+/)?(instructor-documents/[^?]+)");
        Matcher matcher = pattern.matcher(cloudinaryUrl);

        if (matcher.find()) {

            return matcher.group(1);
        }

        log.warn("Could not extract public ID from URL: {}", cloudinaryUrl);
        return null;
    }
}
