package project.ktc.springboot_app.instructor_application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.instructor_application.dto.InstructorApplicationAdminResponseDto;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;

@Component
public class InstructorApplicationsMapper {

        public InstructorApplicationAdminResponseDto toAdminResponseDto(InstructorApplication application) {
                return InstructorApplicationAdminResponseDto.builder()
                                .id(application.getId())
                                .applicant(toUserBasicDto(application.getUser()))
                                .status(application.getStatus())
                                .submittedAt(application.getSubmittedAt())
                                // .rejectionReason(application.getRejectionReason())
                                .build();
        }

        private InstructorApplicationAdminResponseDto.UserBasicDto toUserBasicDto(User user) {
                return InstructorApplicationAdminResponseDto.UserBasicDto.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
        }

        public List<InstructorApplicationAdminResponseDto> toAdminResponseDtoList(
                        List<InstructorApplication> applications) {
                return applications.stream()
                                .map(this::toAdminResponseDto)
                                .toList();
        }
}
