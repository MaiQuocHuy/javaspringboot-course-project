package project.ktc.springboot_app.quiz.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
