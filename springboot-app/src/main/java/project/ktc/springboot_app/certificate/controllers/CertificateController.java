package project.ktc.springboot_app.certificate.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.certificate.dto.CertificateListDto;
import project.ktc.springboot_app.certificate.dto.CertificateResponseDto;
import project.ktc.springboot_app.certificate.dto.CreateCertificateDto;
import project.ktc.springboot_app.certificate.interfaces.CertificateService;
import project.ktc.springboot_app.certificate.services.CertificatePdfService;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Certificate Management API", description = "APIs for managing course completion certificates")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CertificateController {

        private final CertificateService certificateService;
        private final CertificatePdfService certificatePdfService;

        @PostMapping("/sync")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create a new certificate", description = """
                        Creates a new certificate for a user who completed a course.
                        Only users with ADMIN role can create certificates.

                        **Business Rules:**
                        - User and course must exist
                        - User must be enrolled in the course
                        - User must have completed all lessons in the course
                        - Each user can only have one certificate per course
                        - Certificate code must be unique

                        **Generated Certificate Code Format:**
                        [COURSE_PREFIX]-[YEAR]-[SEQUENCE]-[RANDOM_ID]
                        Example: RC-2025-001-ABCD1234
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Certificate created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CertificateResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or user hasn't completed course", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Not Found - User or course not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "409", description = "Conflict - Certificate already exists", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CertificateResponseDto>> createCertificate(
                        @Parameter(description = "Certificate creation request", required = true) @Valid @RequestBody CreateCertificateDto createCertificateDto) {

                log.info("Admin request to create certificate for user {} and course {}",
                                createCertificateDto.getUserId(), createCertificateDto.getCourseId());

                return certificateService.createCertificate(createCertificateDto);
        }

        @PostMapping("/async")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create a new certificate", description = """
                        Creates a new certificate for a user who completed a course.
                        Only users with ADMIN role can create certificates.

                        **Business Rules:**
                        - User and course must exist
                        - User must be enrolled in the course
                        - User must have completed all lessons in the course
                        - Each user can only have one certificate per course
                        - Certificate code must be unique

                        **Generated Certificate Code Format:**
                        [COURSE_PREFIX]-[YEAR]-[SEQUENCE]-[RANDOM_ID]
                        Example: RC-2025-001-ABCD1234
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Certificate created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CertificateResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input or user hasn't completed course", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Not Found - User or course not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "409", description = "Conflict - Certificate already exists", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CertificateResponseDto>> createCertificateAsync(
                        @Parameter(description = "Certificate creation request", required = true) @Valid @RequestBody CreateCertificateDto createCertificateDto) {

                log.info("Admin request to create certificate for user {} and course {}",
                                createCertificateDto.getUserId(), createCertificateDto.getCourseId());

                return certificateService.createCertificateAsync(createCertificateDto);
        }

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all certificates", description = """
                        Retrieves a paginated list of all certificates with optional search filtering.
                        Only ADMIN users can access this endpoint.

                        **Search supports:**
                        - User name and email
                        - Course title
                        - Certificate code
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CertificateListDto>>> getAllCertificates(
                        @Parameter(description = "Search term for filtering certificates", example = "john") @RequestParam(required = false) String search,

                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "issuedAt,desc") @RequestParam(defaultValue = "issuedAt,desc") String sort) {

                log.info("Admin request to get all certificates with search: {}, page: {}, size: {}", search, page,
                                size);

                Pageable pageable = createPageable(page, size, sort);
                return certificateService.getAllCertificates(search, pageable);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN') or @certificateServiceImp.hasAccessToCertificate(#id)")
        @Operation(summary = "Get certificate by ID", description = """
                        Retrieves certificate details by certificate ID.

                        **Access Rules:**
                        - ADMIN: Can access any certificate
                        - STUDENT/INSTRUCTOR: Can only access their own certificates or certificates for courses they teach
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CertificateResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - No access to this certificate", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Certificate not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CertificateResponseDto>> getCertificateById(
                        @Parameter(description = "Certificate ID", required = true, example = "cert-123") @PathVariable String id) {

                log.info("Request to get certificate by ID: {}", id);
                return certificateService.getCertificateById(id);
        }

        @GetMapping("/code/{certificateCode}")
        @Operation(summary = "Get certificate by certificate code", description = """
                        Retrieves certificate details by unique certificate code.
                        This is a public endpoint that doesn't require authentication.
                        Used for certificate verification purposes.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CertificateResponseDto.class))),
                        @ApiResponse(responseCode = "404", description = "Certificate not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CertificateResponseDto>> getCertificateByCertificateCode(
                        @Parameter(description = "Certificate code", required = true, example = "RC-2025-001-ABCD1234") @PathVariable String certificateCode) {

                log.info("Public request to get certificate by code: {}", certificateCode);
                return certificateService.getCertificateByCertificateCode(certificateCode);
        }

        @GetMapping("/user/{userId}")
        @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
        @Operation(summary = "Get certificates by user ID", description = """
                        Retrieves paginated list of certificates for a specific user.

                        **Access Rules:**
                        - ADMIN: Can access any user's certificates
                        - STUDENT/INSTRUCTOR: Can only access their own certificates
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User certificates retrieved successfully", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - No access to user's certificates", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByUserId(
                        @Parameter(description = "User ID", required = true, example = "user-123") @PathVariable String userId,

                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "issuedAt,desc") @RequestParam(defaultValue = "issuedAt,desc") String sort) {

                log.info("Request to get certificates for user: {}", userId);

                Pageable pageable = createPageable(page, size, sort);
                return certificateService.getCertificatesByUserId(userId, pageable);
        }

        @GetMapping("/course/{courseId}")
        @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or @courseRepository.existsByIdAndInstructorId(#courseId, authentication.name)")
        @Operation(summary = "Get certificates by course ID", description = """
                        Retrieves paginated list of certificates for a specific course.

                        **Access Rules:**
                        - ADMIN: Can access certificates for any course
                        - INSTRUCTOR: Can only access certificates for courses they teach
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course certificates retrieved successfully", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "403", description = "Forbidden - No access to course certificates", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByCourseId(
                        @Parameter(description = "Course ID", required = true, example = "course-456") @PathVariable String courseId,

                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "issuedAt,desc") @RequestParam(defaultValue = "issuedAt,desc") String sort) {

                log.info("Request to get certificates for course: {}", courseId);

                Pageable pageable = createPageable(page, size, sort);
                return certificateService.getCertificatesByCourseId(courseId, pageable);
        }

        @GetMapping("/my-certificates")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get current user's certificates", description = """
                        Retrieves paginated list of certificates for the currently authenticated user.
                        Available to all authenticated users (STUDENT, INSTRUCTOR, ADMIN).
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User certificates retrieved successfully", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CertificateListDto>>> getMyCertificates(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "issuedAt,desc") @RequestParam(defaultValue = "issuedAt,desc") String sort) {

                log.info("Request to get current user's certificates");

                Pageable pageable = createPageable(page, size, sort);
                return certificateService.getMyCertificates(pageable);
        }

        /**
         * Helper method to create Pageable with sorting
         */
        private Pageable createPageable(int page, int size, String sort) {
                String[] sortParts = sort.split(",");
                String sortField = sortParts.length > 0 ? sortParts[0] : "issuedAt";
                String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";

                Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                return PageRequest.of(page, size, Sort.by(direction, sortField));
        }

        // Test endpoints for PDF generation
        @GetMapping("/test-pdf")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Test PDF generation", description = "Test endpoint to verify html-to-image API integration")
        public ResponseEntity<byte[]> testPdfGeneration() {
                log.info("Testing PDF generation via html-to-image API");

                try {
                        byte[] pdfBytes = certificatePdfService.testSimplePdf();

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment", "test-certificate.pdf");
                        headers.setContentLength(pdfBytes.length);

                        log.info("Test PDF generated successfully. Size: {} bytes", pdfBytes.length);
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(pdfBytes);

                } catch (Exception e) {
                        log.error("Failed to generate test PDF: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError().build();
                }
        }
}
