package project.ktc.springboot_app.instructor_application.dto;

import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewApplicationRequestDto {
    @NotNull
    private ApplicationStatus action;

    private String rejectionReason; // chỉ cần khi REJECTED

}
