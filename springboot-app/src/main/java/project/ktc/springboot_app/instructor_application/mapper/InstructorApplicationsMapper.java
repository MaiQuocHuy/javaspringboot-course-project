package project.ktc.springboot_app.instructor_application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.instructor_application.dto.AdminApplicationDetailDto;
import project.ktc.springboot_app.instructor_application.dto.AdminInstructorApplicationResponseDto;
import project.ktc.springboot_app.instructor_application.dto.InstructorApplicationDetailResponseDto;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;

@Component
public class InstructorApplicationsMapper {

        public AdminInstructorApplicationResponseDto toAdminResponseDto(InstructorApplication application) {
                return AdminInstructorApplicationResponseDto.builder()
                                .id(application.getId())
                                .applicant(toUserBasicDto(application.getUser()))
                                .status(application.getStatus())
                                .submittedAt(application.getSubmittedAt())
                                .build();
        }

        private AdminInstructorApplicationResponseDto.UserBasicDto toUserBasicDto(User user) {
                return AdminInstructorApplicationResponseDto.UserBasicDto.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
        }

        public List<AdminInstructorApplicationResponseDto> toAdminResponseDtoList(
                        List<InstructorApplication> applications) {
                return applications.stream()
                                .map(this::toAdminResponseDto)
                                .toList();
        }

        public AdminApplicationDetailDto toAdminDetailResponseDto(InstructorApplication application) {
                return AdminApplicationDetailDto.builder()
                                .id(application.getId())
                                .applicant(toUserDetailDto(application.getUser()))
                                .status(application.getStatus())
                                .documents(application.getDocuments())
                                .rejectionReason(application.getRejectionReason())
                                .submittedAt(application.getSubmittedAt())
                                .build();
        }

        public InstructorApplicationDetailResponseDto toApplicationDetailResponseDto(
                        InstructorApplication application) {
                return InstructorApplicationDetailResponseDto.builder()
                                .id(application.getId())
                                // .applicant(toUserDetailDto(application.getUser()))
                                .status(application.getStatus())
                                .documents(application.getDocuments())
                                .rejectionReason(application.getRejectionReason())
                                .submittedAt(application.getSubmittedAt())
                                .build();
        }

        private AdminApplicationDetailDto.UserBasicDto toUserDetailDto(User user) {
                return AdminApplicationDetailDto.UserBasicDto.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .thumbnailUrl(user.getThumbnailUrl())
                                .build();
        }
}
