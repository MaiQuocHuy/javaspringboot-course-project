package project.ktc.springboot_app.instructor_application.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.instructor_application.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.instructor_application.services.InstructorApplicationServiceImp;

/**
 * REST Controller for handling instructor application operations
 */
@RestController
@RequestMapping("/api/instructor-applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Instructor Application API", description = "Endpoints for instructor application management")
@SecurityRequirement(name = "bearerAuth")
public class InstructorApplicationController {

    private final InstructorApplicationServiceImp instructorApplicationService;

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Upload instructor application documents", description = """
            Upload required documents to apply for the instructor role.
            Documents are stored on Cloudinary and recorded as a JSON key-value structure.

            **Business Rules:**
            - Each user can submit only once, unless their previous application was REJECTED
            - If an application is rejected, the user is allowed to resubmit only once, within 3 days from the rejection date
            - If the user is rejected twice or the resubmission period expires, further submissions are not allowed
            - While the user's application status is PENDING, they are not allowed to purchase courses or perform actions restricted to regular STUDENT roles

            **Validation Rules:**
            - Certificate file: Required (PDF, DOCX, PNG, JPG - max 15MB)
            - Portfolio: Required valid URL (GitHub, LinkedIn, or demo portfolio)
            - CV file: Required (PDF, DOCX, JPG - max 15MB)
            - Other file: Optional additional supporting documents (max 15MB)

            **Returns:** User ID and uploaded document URLs on successful upload.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents uploaded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed - invalid files, ineligible user, or missing required fields", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Forbidden - user does not have STUDENT role", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Upload failed due to server error", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<DocumentUploadResponseDto>> uploadDocuments(
            @Parameter(description = "Professional certification file (PDF, DOCX, PNG, JPG - max 15MB)", required = true) @RequestParam("certificate") MultipartFile certificate,

            @Parameter(description = "Link to GitHub, LinkedIn, or demo portfolio", required = true) @RequestParam("portfolio") String portfolio,

            @Parameter(description = "Resume/CV file (PDF, DOCX, JPG - max 15MB)", required = true) @RequestParam("cv") MultipartFile cv,

            @Parameter(description = "Additional supporting documents (e.g., awards, projects, etc.)", required = false) @RequestParam(value = "other", required = false) MultipartFile other) {

        log.info(
                "Received document upload request for instructor application - Certificate: {}, Portfolio: {}, CV: {}, Other: {}",
                certificate != null ? certificate.getOriginalFilename() : "null",
                portfolio,
                cv != null ? cv.getOriginalFilename() : "null",
                other != null ? other.getOriginalFilename() : "null");

        return instructorApplicationService.uploadDocuments(certificate, portfolio, cv, other);
    }
}
