package project.ktc.springboot_app.instructor_application.interfaces;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.instructor_application.dto.AdminInstructorApplicationResponseDto;
import project.ktc.springboot_app.instructor_application.dto.AdminReviewApplicationRequestDto;
import project.ktc.springboot_app.instructor_application.dto.DocumentUploadResponseDto;
import project.ktc.springboot_app.instructor_application.dto.InstructorApplicationDetailResponseDto;

/** Service interface for instructor application operations */
public interface InstructorApplicationService {

	/**
	 * Upload documents for instructor application
	 *
	 * @param certificate
	 *            Professional certification file (required)
	 * @param portfolio
	 *            Portfolio URL (required)
	 * @param cv
	 *            Resume/CV file (required)
	 * @param other
	 *            Additional supporting documents (optional)
	 * @return ResponseEntity with upload result
	 */
	ResponseEntity<ApiResponse<DocumentUploadResponseDto>> uploadDocuments(
			MultipartFile certificate, String portfolio, MultipartFile cv, MultipartFile other);

	ResponseEntity<ApiResponse<List<AdminInstructorApplicationResponseDto>>> getAllApplicationAdmin();

	ResponseEntity<ApiResponse<List<AdminInstructorApplicationResponseDto>>> getApplicationByUserIdAdmin(String userId);

	ResponseEntity<ApiResponse<Void>> reviewApplication(
			String id, AdminReviewApplicationRequestDto request);

	ResponseEntity<ApiResponse<InstructorApplicationDetailResponseDto>> getApplicationByUserId(
			String userId);

	ResponseEntity<ApiResponse<Void>> deleteApplicationById(String applicationId);
}
