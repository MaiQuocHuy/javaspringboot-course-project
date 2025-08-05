package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {

    private String id;
    private String title;
    private String description;
    private String courseId;
    private String lessonId;
    private List<QuizQuestionResponseDto> questions;
    private LocalDateTime createdAt;
}
