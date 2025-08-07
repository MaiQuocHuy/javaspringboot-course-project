package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizScoreDetailResponseDto {
    private String id;
    private LessonSummary lesson;
    private SectionSummary section;
    private CourseSummary course;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private LocalDateTime completedAt;
    private Boolean canReview;
    private List<QuestionDetail> questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonSummary {
        private String id;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionSummary {
        private String id;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseSummary {
        private String id;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDetail {
        private String id;
        private String questionText;
        private List<String> options;
        private String studentAnswer;
        private String correctAnswer;
        private Boolean isCorrect;
        private String explanation;
    }
}
