package project.ktc.springboot_app.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.quiz.dto.QuizQuestionResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonWithQuizResponseDto {

    private LessonResponseDto lesson;
    private QuizWithinLessonResponseDto quiz;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonResponseDto {
        private String id;
        private String title;
        private String description;
        private String sectionId;
        private Integer orderIndex;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizWithinLessonResponseDto {
        private String id; // This will be the lesson ID (same as lesson)
        private String title;
        private String description;
        private List<QuizQuestionResponseDto> questions;
    }
}
