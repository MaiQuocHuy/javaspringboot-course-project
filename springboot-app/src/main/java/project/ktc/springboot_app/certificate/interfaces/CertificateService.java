package project.ktc.springboot_app.certificate.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.certificate.dto.CertificateListDto;
import project.ktc.springboot_app.certificate.dto.CertificateResponseDto;
import project.ktc.springboot_app.certificate.dto.CreateCertificateDto;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

public interface CertificateService {

	/**
	 * Create a new certificate for a user who completed a course Only ADMIN users
	 * can create
	 * certificates
	 *
	 * @param createCertificateDto
	 *            Certificate creation data
	 * @return Created certificate details
	 */
	ResponseEntity<ApiResponse<CertificateResponseDto>> createCertificate(
			CreateCertificateDto createCertificateDto);

	/**
	 * Get all certificates with pagination and optional search Only ADMIN users can
	 * access this
	 * endpoint
	 *
	 * @param search
	 *            Optional search term for filtering
	 * @param pageable
	 *            Pagination parameters
	 * @return Paginated list of certificates
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getAllCertificates(
			String search, Pageable pageable);

	/**
	 * Get certificate details by ID ADMIN users can access any certificate
	 * STUDENT/INSTRUCTOR users
	 * can only access their own certificates
	 *
	 * @param certificateId
	 *            Certificate ID
	 * @return Certificate details
	 */
	ResponseEntity<ApiResponse<CertificateResponseDto>> getCertificateById(String certificateId);

	/**
	 * Get certificate by certificate code Public endpoint - no authentication
	 * required
	 *
	 * @param certificateCode
	 *            Unique certificate code
	 * @return Certificate details
	 */
	ResponseEntity<ApiResponse<CertificateResponseDto>> getCertificateByCertificateCode(
			String certificateCode);

	/**
	 * Get certificates for a specific user ADMIN users can access any user's
	 * certificates
	 * STUDENT/INSTRUCTOR users can only access their own certificates
	 *
	 * @param userId
	 *            User ID
	 * @param pageable
	 *            Pagination parameters
	 * @return Paginated list of user's certificates
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByUserId(
			String userId, Pageable pageable);

	/**
	 * Get certificates for a specific course Only ADMIN and course INSTRUCTOR can
	 * access this
	 * endpoint
	 *
	 * @param courseId
	 *            Course ID
	 * @param pageable
	 *            Pagination parameters
	 * @return Paginated list of course certificates
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByCourseId(
			String courseId, Pageable pageable);

	/**
	 * Get current user's certificates Returns certificates for the authenticated
	 * user
	 *
	 * @param pageable
	 *            Pagination parameters
	 * @return Paginated list of current user's certificates
	 */
	ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getMyCertificates(
			Pageable pageable);

	/**
	 * Download certificate PDF Available to certificate owner, course instructor,
	 * and ADMIN users
	 *
	 * @param certificateId
	 *            Certificate ID
	 * @return PDF file as ResponseEntity
	 */
	ResponseEntity<?> downloadCertificatePdf(String certificateId);

	/**
	 * Regenerate certificate PDF Only ADMIN users can regenerate PDFs
	 *
	 * @param certificateId
	 *            Certificate ID
	 * @return Updated certificate details
	 */
	ResponseEntity<ApiResponse<CertificateResponseDto>> regenerateCertificatePdf(
			String certificateId);

	/**
	 * Check if current user has access to the certificate Used for security
	 * authorization
	 *
	 * @param certificateId
	 *            Certificate ID
	 * @return true if current user has access, false otherwise
	 */
	boolean hasAccessToCertificate(String certificateId);

	/**
	 * Create a new certificate asynchronously for a user who completed a course
	 * Only ADMIN users can
	 * create certificates The certificate creation process runs in background and
	 * includes: -
	 * Validation of user and course - PDF generation - Cloud storage upload - Email
	 * notification
	 *
	 * @param createCertificateDto
	 *            Certificate creation data
	 * @return Immediate response with certificate basic info (PDF will be generated
	 *         asynchronously)
	 */
	ResponseEntity<ApiResponse<CertificateResponseDto>> createCertificateAsync(
			CreateCertificateDto createCertificateDto);
}
