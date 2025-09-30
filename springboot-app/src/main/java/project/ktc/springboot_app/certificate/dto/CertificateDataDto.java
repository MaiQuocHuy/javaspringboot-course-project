package project.ktc.springboot_app.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO containing all data needed for certificate PDF generation */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Certificate data for PDF generation")
public class CertificateDataDto {

	@Schema(description = "Full name of the student", example = "John Doe")
	private String studentName;

	@Schema(description = "Email of the student", example = "john.doe@example.com")
	private String studentEmail;

	@Schema(description = "Title of the completed course", example = "React Development Masterclass")
	private String courseTitle;

	@Schema(description = "Name of the course instructor", example = "Jane Smith")
	private String instructorName;

	@Schema(description = "Date when the certificate was issued")
	private LocalDateTime issueDate;

	@Schema(description = "Unique certificate code", example = "RC-2025-001-ABCD1234")
	private String certificateCode;

	@Schema(description = "Course level", example = "Intermediate")
	private String courseLevel;

	@Schema(description = "Course duration", example = "8 weeks")
	private String courseDuration;

	@Schema(description = "Number of lessons in the course", example = "25")
	private Integer lessonCount;
}
