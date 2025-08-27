package project.ktc.springboot_app.instructor_application.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication.ApplicationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorApplicationDetailResponseDto {

    private String id;

    private ApplicationStatus status;

    private String documents;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    private String rejectionReason;

    private long submitAttemptRemain;

}
