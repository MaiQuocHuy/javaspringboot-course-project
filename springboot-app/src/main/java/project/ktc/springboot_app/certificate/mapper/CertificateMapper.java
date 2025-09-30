package project.ktc.springboot_app.certificate.mapper;

import org.springframework.stereotype.Component;
import project.ktc.springboot_app.certificate.dto.CertificateListDto;
import project.ktc.springboot_app.certificate.dto.CertificateResponseDto;
import project.ktc.springboot_app.certificate.entity.Certificate;

/**
 * Mapper for Certificate entity to DTOs Provides clean separation between
 * entity and DTO conversion
 * logic
 */
@Component
public class CertificateMapper {

	/** Convert Certificate entity to CertificateResponseDto */
	public CertificateResponseDto toResponseDto(Certificate certificate) {
		if (certificate == null) {
			return null;
		}

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
								.instructorName(
										certificate.getCourse().getInstructor() != null
												? certificate.getCourse().getInstructor().getName()
												: "Unknown")
								.build())
				.build();
	}

	/** Convert Certificate entity to CertificateListDto */
	public CertificateListDto toListDto(Certificate certificate) {
		if (certificate == null) {
			return null;
		}

		return CertificateListDto.builder()
				.id(certificate.getId())
				.certificateCode(certificate.getCertificateCode())
				.issuedAt(certificate.getIssuedAt())
				.userName(certificate.getUser().getName())
				.userEmail(certificate.getUser().getEmail())
				.courseTitle(certificate.getCourse().getTitle())
				.instructorName(
						certificate.getCourse().getInstructor() != null
								? certificate.getCourse().getInstructor().getName()
								: "Unknown")
				.fileStatus(certificate.getFileUrl() != null ? "GENERATED" : "PENDING")
				.build();
	}
}
