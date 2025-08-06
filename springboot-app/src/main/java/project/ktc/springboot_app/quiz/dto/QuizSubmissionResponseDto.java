package project.ktc.springboot_app.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResponseDto {

    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private String feedback;
    private LocalDateTime submittedAt;
}
