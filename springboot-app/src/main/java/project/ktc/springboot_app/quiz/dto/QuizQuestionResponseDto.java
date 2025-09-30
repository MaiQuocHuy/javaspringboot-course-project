package project.ktc.springboot_app.quiz.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponseDto {

	private String id;
	private String questionText;
	private Map<String, String> options;
	private String correctAnswer;
	private String explanation;
}
