package project.ktc.springboot_app.certificate.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.certificate.dto.CertificateListDto;
import project.ktc.springboot_app.certificate.dto.CertificateResponseDto;
import project.ktc.springboot_app.certificate.dto.CreateCertificateDto;
import project.ktc.springboot_app.certificate.entity.Certificate;
import project.ktc.springboot_app.certificate.interfaces.CertificateService;
import project.ktc.springboot_app.certificate.repositories.CertificateRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImp implements CertificateService {

	private final CertificateRepository certificateRepository;
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final CertificateAsyncService certificateAsyncService;

	@Override
	@Transactional
	public ResponseEntity<ApiResponse<CertificateResponseDto>> createCertificate(
			CreateCertificateDto createCertificateDto) {
		log.info(
				"Creating certificate for user {} and course {}",
				createCertificateDto.getUserId(),
				createCertificateDto.getCourseId());

		try {
			// 1. Validate user exists
			Optional<User> userOpt = userRepository.findById(createCertificateDto.getUserId());
			if (userOpt.isEmpty()) {
				return ApiResponseUtil.notFound(
						"User not found with id: " + createCertificateDto.getUserId());
			}
			User user = userOpt.get();

			// 2. Validate course exists
			Optional<Course> courseOpt = courseRepository.findById(createCertificateDto.getCourseId());
			if (courseOpt.isEmpty()) {
				return ApiResponseUtil.notFound(
						"Course not found with id: " + createCertificateDto.getCourseId());
			}
			Course course = courseOpt.get();

			// 3. Check if certificate already exists
			if (certificateRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
				return ApiResponseUtil.conflict("Certificate already exists for this user and course");
			}

			// 4. Validate user completed all lessons in the course
			if (!hasUserCompletedAllLessons(user.getId(), course.getId())) {
				return ApiResponseUtil.badRequest("User has not completed all lessons in the course");
			}

			// 5. Validate user is enrolled in the course
			if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
				return ApiResponseUtil.badRequest("User is not enrolled in the course");
			}

			// 6. Generate unique certificate code
			String certificateCode = generateUniqueCertificateCode(course);

			// 7. Create certificate entity
			Certificate certificate = new Certificate();
			// certificate.setCertificateId(UUID.randomUUID().toString());
			certificate.setUser(user);
			certificate.setCourse(course);
			certificate.setCertificateCode(certificateCode);
			certificate.setIssuedAt(LocalDateTime.now());

			// 8. Save certificate first (without file URL)
			Certificate savedCertificate = certificateRepository.save(certificate);

			log.info(
					"Certificate created successfully with ID: {} and code: {}",
					savedCertificate.getId(),
					savedCertificate.getCertificateCode());

			// 9. Process PDF generation, upload, and email notification asynchronously
			// Pass only the ID to avoid entity detachment issues
			// try {
			// certificateAsyncService.processCertificateAsync(savedCertificate.getId());
			// } catch (Exception e) {
			// log.error("Failed to start async processing for certificate {}: {}",
			// savedCertificate.getId(), e.getMessage(), e);
			// // Don't fail the certificate creation, just log the error
			// }

			// 10. Build response
			CertificateResponseDto responseDto = mapToCertificateResponseDto(savedCertificate);

			// 5. Schedule async processing AFTER transaction commits
			scheduleAsyncProcessing(savedCertificate.getId());

			return ApiResponseUtil.created(responseDto, "Certificate created successfully");

		} catch (Exception e) {
			log.error("Error creating certificate: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError("Failed to create certificate: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<CertificateResponseDto>> createCertificateAsync(
			CreateCertificateDto createCertificateDto) {
		log.info(
				"Creating certificate asynchronously for user {} and course {}",
				createCertificateDto.getUserId(),
				createCertificateDto.getCourseId());

		try {
			// 1. Validate user exists
			Optional<User> userOpt = userRepository.findById(createCertificateDto.getUserId());
			if (userOpt.isEmpty()) {
				return ApiResponseUtil.notFound(
						"User not found with id: " + createCertificateDto.getUserId());
			}
			User user = userOpt.get();

			// 2. Validate course exists
			Optional<Course> courseOpt = courseRepository.findById(createCertificateDto.getCourseId());
			if (courseOpt.isEmpty()) {
				return ApiResponseUtil.notFound(
						"Course not found with id: " + createCertificateDto.getCourseId());
			}
			Course course = courseOpt.get();

			// 3. Check if certificate already exists
			if (certificateRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
				return ApiResponseUtil.conflict("Certificate already exists for this user and course");
			}

			// 4. Validate user completed all lessons in the course
			if (!hasUserCompletedAllLessons(user.getId(), course.getId())) {
				return ApiResponseUtil.badRequest("User has not completed all lessons in the course");
			}

			// 5. Validate user is enrolled in the course
			if (!enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
				return ApiResponseUtil.badRequest("User is not enrolled in the course");
			}

			// 6. Generate unique certificate code
			String certificateCode = generateUniqueCertificateCode(course);

			// 7. Create certificate entity
			Certificate certificate = new Certificate();
			certificate.setUser(user);
			certificate.setCourse(course);
			certificate.setCertificateCode(certificateCode);
			certificate.setIssuedAt(LocalDateTime.now());

			// 8. Save certificate first (without file URL)
			Certificate savedCertificate = certificateRepository.save(certificate);

			log.info(
					"Certificate created successfully with ID: {} and code: {}",
					savedCertificate.getId(),
					savedCertificate.getCertificateCode());

			// 9. Process PDF generation, upload, and email notification asynchronously
			// Pass only the ID to avoid entity detachment issues
			certificateAsyncService.processCertificateAsync(savedCertificate.getId());

			// 10. Build response
			CertificateResponseDto responseDto = mapToCertificateResponseDto(savedCertificate);

			return ApiResponseUtil.created(
					responseDto,
					"Certificate created successfully. PDF generation and email notification are being processed in the background.");

		} catch (Exception e) {
			log.error("Error creating certificate asynchronously: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError("Failed to create certificate: " + e.getMessage());
		}
	}

	/**
	 * Process certificate PDF generation, cloud upload, and email notification
	 * asynchronously This
	 * method runs in a separate thread and has its own transaction context
	 *
	 * @param certificateId
	 *            The ID of the certificate to process
	 */
	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getAllCertificates(
			String search, Pageable pageable) {
		log.info("Getting all certificates with search: {} and pagination: {}", search, pageable);

		try {
			Page<Certificate> certificatesPage = certificateRepository.findAllWithSearch(search, pageable);

			List<CertificateListDto> certificateList = certificatesPage.getContent().stream()
					.map(this::mapToCertificateListDto)
					.collect(Collectors.toList());

			PaginatedResponse<CertificateListDto> paginatedResponse = PaginatedResponse.<CertificateListDto>builder()
					.content(certificateList)
					.page(
							PaginatedResponse.PageInfo.builder()
									.number(certificatesPage.getNumber())
									.size(certificatesPage.getSize())
									.totalPages(certificatesPage.getTotalPages())
									.totalElements(certificatesPage.getTotalElements())
									.first(certificatesPage.isFirst())
									.last(certificatesPage.isLast())
									.build())
					.build();

			return ApiResponseUtil.success(paginatedResponse, "Certificates retrieved successfully");

		} catch (Exception e) {
			log.error("Error getting certificates: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve certificates: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<CertificateResponseDto>> getCertificateById(
			String certificateId) {
		log.info("Getting certificate by ID: {}", certificateId);

		try {
			Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);
			if (certificateOpt.isEmpty()) {
				return ApiResponseUtil.notFound("Certificate not found with id: " + certificateId);
			}

			Certificate certificate = certificateOpt.get();

			// Check access permissions
			String currentUserId = SecurityUtil.getCurrentUserId();
			if (!hasAccessToCertificate(certificate, currentUserId)) {
				return ApiResponseUtil.forbidden("You do not have permission to access this certificate");
			}

			CertificateResponseDto responseDto = mapToCertificateResponseDto(certificate);

			return ApiResponseUtil.success(responseDto, "Certificate retrieved successfully");

		} catch (Exception e) {
			log.error("Error getting certificate by ID: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve certificate: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<CertificateResponseDto>> getCertificateByCertificateCode(
			String certificateCode) {
		log.info("Getting certificate by code: {}", certificateCode);

		try {
			Optional<Certificate> certificateOpt = certificateRepository.findByCertificateCode(certificateCode);
			if (certificateOpt.isEmpty()) {
				return ApiResponseUtil.notFound("Certificate not found with code: " + certificateCode);
			}

			Certificate certificate = certificateOpt.get();
			CertificateResponseDto responseDto = mapToCertificateResponseDto(certificate);

			return ApiResponseUtil.success(responseDto, "Certificate retrieved successfully");

		} catch (Exception e) {
			log.error("Error getting certificate by code: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve certificate: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByUserId(
			String userId, Pageable pageable) {
		log.info("Getting certificates for user: {}", userId);

		try {
			// Check if user exists
			if (!userRepository.existsById(userId)) {
				return ApiResponseUtil.notFound("User not found with id: " + userId);
			}

			// Check access permissions
			String currentUserId = SecurityUtil.getCurrentUserId();
			if (!hasAccessToUserCertificates(userId, currentUserId)) {
				return ApiResponseUtil.forbidden("You do not have permission to access these certificates");
			}

			Page<Certificate> certificatesPage = certificateRepository.findByUserId(userId, pageable);

			List<CertificateListDto> certificateList = certificatesPage.getContent().stream()
					.map(this::mapToCertificateListDto)
					.collect(Collectors.toList());

			PaginatedResponse<CertificateListDto> paginatedResponse = PaginatedResponse.<CertificateListDto>builder()
					.content(certificateList)
					.page(
							PaginatedResponse.PageInfo.builder()
									.number(certificatesPage.getNumber())
									.size(certificatesPage.getSize())
									.totalPages(certificatesPage.getTotalPages())
									.totalElements(certificatesPage.getTotalElements())
									.first(certificatesPage.isFirst())
									.last(certificatesPage.isLast())
									.build())
					.build();

			return ApiResponseUtil.success(paginatedResponse, "User certificates retrieved successfully");

		} catch (Exception e) {
			log.error("Error getting certificates for user: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve user certificates: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getCertificatesByCourseId(String courseId,
			Pageable pageable) {
		log.info("Getting certificates for course: {}", courseId);

		try {
			// Check if course exists
			Optional<Course> courseOpt = courseRepository.findById(courseId);
			if (courseOpt.isEmpty()) {
				return ApiResponseUtil.notFound("Course not found with id: " + courseId);
			}
			Course course = courseOpt.get();

			// Check access permissions (Admin or course instructor)
			String currentUserId = SecurityUtil.getCurrentUserId();
			if (!hasAccessToCourseCertificates(course, currentUserId)) {
				return ApiResponseUtil.forbidden("You do not have permission to access these certificates");
			}

			Page<Certificate> certificatesPage = certificateRepository.findByCourseId(courseId, pageable);

			List<CertificateListDto> certificateList = certificatesPage.getContent().stream()
					.map(this::mapToCertificateListDto)
					.collect(Collectors.toList());

			PaginatedResponse<CertificateListDto> paginatedResponse = PaginatedResponse.<CertificateListDto>builder()
					.content(certificateList)
					.page(
							PaginatedResponse.PageInfo.builder()
									.number(certificatesPage.getNumber())
									.size(certificatesPage.getSize())
									.totalPages(certificatesPage.getTotalPages())
									.totalElements(certificatesPage.getTotalElements())
									.first(certificatesPage.isFirst())
									.last(certificatesPage.isLast())
									.build())
					.build();

			return ApiResponseUtil.success(
					paginatedResponse, "Course certificates retrieved successfully");

		} catch (Exception e) {
			log.error("Error getting certificates for course: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve course certificates: " + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponse<PaginatedResponse<CertificateListDto>>> getMyCertificates(
			Pageable pageable) {
		String currentUserId = SecurityUtil.getCurrentUserId();
		log.info("Getting certificates for current user: {}", currentUserId);

		return getCertificatesByUserId(currentUserId, pageable);
	}

	@Override
	public ResponseEntity<?> downloadCertificatePdf(String certificateId) {
		log.info("Downloading PDF for certificate ID: {}", certificateId);

		try {
			// Get certificate
			Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);
			if (certificateOpt.isEmpty()) {
				return ApiResponseUtil.notFound("Certificate not found with id: " + certificateId);
			}

			Certificate certificate = certificateOpt.get();

			// Check access permissions
			String currentUserId = SecurityUtil.getCurrentUserId();
			if (!hasAccessToCertificate(certificate, currentUserId)) {
				return ApiResponseUtil.forbidden("You do not have permission to download this certificate");
			}

			// Check if PDF is available
			if (certificate.getFileUrl() == null || certificate.getFileUrl().isEmpty()) {
				return ApiResponseUtil.notFound(
						"Certificate PDF is not available. Please contact support.");
			}

			// Return redirect to download URL with proper headers
			return ResponseEntity.status(302)
					.header("Location", certificate.getFileUrl())
					.header(
							"Content-Disposition",
							"attachment; filename=\"certificate_" + certificate.getCertificateCode() + ".pdf\"")
					.build();

		} catch (Exception e) {
			log.error("Error downloading certificate PDF: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to download certificate: " + e.getMessage());
		}
	}

	@Override
	@Transactional
	public ResponseEntity<ApiResponse<CertificateResponseDto>> regenerateCertificatePdf(
			String certificateId) {
		log.info("Regenerating PDF for certificate ID: {}", certificateId);

		try {
			// Get certificate
			Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);
			if (certificateOpt.isEmpty()) {
				return ApiResponseUtil.notFound("Certificate not found with id: " + certificateId);
			}

			Certificate certificate = certificateOpt.get();

			// Clear existing file URL
			certificate.setFileUrl(null);
			certificateRepository.save(certificate);

			// Regenerate image asynchronously
			certificateAsyncService.processCertificateAsync(certificate.getId());

			// Build response
			CertificateResponseDto responseDto = mapToCertificateResponseDto(certificate);

			return ApiResponseUtil.success(responseDto, "Certificate image regeneration started");

		} catch (Exception e) {
			log.error("Error regenerating certificate PDF: {}", e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to regenerate certificate PDF: " + e.getMessage());
		}
	}

	@Override
	public boolean hasAccessToCertificate(String certificateId) {
		try {
			Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);
			if (certificateOpt.isEmpty()) {
				return false;
			}

			Certificate certificate = certificateOpt.get();
			String currentUserId = SecurityUtil.getCurrentUserId();

			return hasAccessToCertificate(certificate, currentUserId);

		} catch (Exception e) {
			log.error("Error checking certificate access: {}", e.getMessage(), e);
			return false;
		}
	}

	// Private helper methods

	/** Check if user has completed all lessons in the course */
	private boolean hasUserCompletedAllLessons(String userId, String courseId) {
		try {
			log.debug("Checking if user {} has completed all lessons in course {}", userId, courseId);

			// Count total lessons in the course
			Long totalLessons = enrollmentRepository.countTotalLessonsByCourse(courseId);

			// Count completed lessons by the user in this course
			Long completedLessons = enrollmentRepository.countCompletedLessonsByUserAndCourse(userId, courseId);

			log.debug(
					"Course {} - Total lessons: {}, Completed by user {}: {}",
					courseId,
					totalLessons,
					userId,
					completedLessons);

			// Return true if user has completed all lessons (and there are lessons to
			// complete)
			return totalLessons > 0 && completedLessons.equals(totalLessons);

		} catch (Exception e) {
			log.error(
					"Error checking lesson completion for user {} in course {}: {}",
					userId,
					courseId,
					e.getMessage(),
					e);
			return false;
		}
	}

	/** Generate unique certificate code based on course and timestamp */
	private String generateUniqueCertificateCode(Course course) {
		String coursePrefix = generateCoursePrefix(course.getTitle());
		String year = String.valueOf(LocalDateTime.now().getYear());

		String baseCode;
		int sequence = 1;

		do {
			String sequenceStr = String.format("%03d", sequence);
			String randomSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
			baseCode = String.format("%s-%s-%s-%s", coursePrefix, year, sequenceStr, randomSuffix);
			sequence++;
		} while (certificateRepository.existsByCertificateCode(baseCode));

		return baseCode;
	}

	/** Generate course prefix from course title */
	private String generateCoursePrefix(String courseTitle) {
		if (courseTitle == null || courseTitle.trim().isEmpty()) {
			return "GEN";
		}

		// Extract meaningful words and create prefix
		String[] words = courseTitle.trim().split("\\s+");
		StringBuilder prefix = new StringBuilder();

		for (String word : words) {
			if (word.length() > 0 && prefix.length() < 4) {
				prefix.append(word.charAt(0));
			}
		}

		return prefix.toString().toUpperCase();
	}

	/** Generate certificate image and upload to cloud storage asynchronously */

	/** Check if current user has access to the certificate */
	private boolean hasAccessToCertificate(Certificate certificate, String currentUserId) {
		try {
			// Admin can access any certificate
			if (SecurityUtil.hasRole("ADMIN")) {
				return true;
			}

			// User can access their own certificate
			if (certificate.getUser().getId().equals(currentUserId)) {
				return true;
			}

			// Course instructor can access certificates for their course
			if (certificate.getCourse().getInstructor().getId().equals(currentUserId)) {
				return true;
			}

			return false;
		} catch (Exception e) {
			log.error("Error checking certificate access: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Check if current user has access to user's certificates */
	private boolean hasAccessToUserCertificates(String userId, String currentUserId) {
		try {
			// Admin can access any user's certificates
			if (SecurityUtil.hasRole("ADMIN")) {
				return true;
			}

			// User can access their own certificates
			return userId.equals(currentUserId);
		} catch (Exception e) {
			log.error("Error checking user certificates access: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Check if current user has access to course certificates */
	private boolean hasAccessToCourseCertificates(Course course, String currentUserId) {
		try {
			// Admin can access any course certificates
			if (SecurityUtil.hasRole("ADMIN")) {
				return true;
			}

			// Course instructor can access certificates for their course
			return course.getInstructor().getId().equals(currentUserId);
		} catch (Exception e) {
			log.error("Error checking course certificates access: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Map Certificate entity to CertificateResponseDto */
	private CertificateResponseDto mapToCertificateResponseDto(Certificate certificate) {
		return CertificateResponseDto.builder()
				.id(certificate.getId())
				.certificateCode(certificate.getCertificateCode())
				.issuedAt(certificate.getIssuedAt())
				.fileUrl(certificate.getFileUrl())
				.user(
						CertificateResponseDto.UserInfo.builder()
								.id(certificate.getUser().getId())
								.name(certificate.getUser().getName())
								.email(certificate.getUser().getEmail())
								.build())
				.course(
						CertificateResponseDto.CourseInfo.builder()
								.id(certificate.getCourse().getId())
								.title(certificate.getCourse().getTitle())
								.instructorName(certificate.getCourse().getInstructor().getName())
								.build())
				.build();
	}

	/** Map Certificate entity to CertificateListDto */
	private CertificateListDto mapToCertificateListDto(Certificate certificate) {
		return CertificateListDto.builder()
				.id(certificate.getId())
				.certificateCode(certificate.getCertificateCode())
				.issuedAt(certificate.getIssuedAt())
				.userName(certificate.getUser().getName())
				.userEmail(certificate.getUser().getEmail())
				.courseTitle(certificate.getCourse().getTitle())
				.instructorName(certificate.getCourse().getInstructor().getName())
				.fileStatus(certificate.getFileUrl() != null ? "GENERATED" : "PENDING")
				.build();
	}

	private void scheduleAsyncProcessing(String certificateId) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
					new TransactionSynchronization() {
						@Override
						public void afterCommit() {
							try {
								certificateAsyncService.processCertificateAsync(certificateId);
							} catch (Exception e) {
								log.error("Failed to start async processing: {}", e.getMessage());
							}
						}
					});
		} else {
			// Fallback nếu không có active transaction
			certificateAsyncService.processCertificateAsync(certificateId);
		}
	}
}
