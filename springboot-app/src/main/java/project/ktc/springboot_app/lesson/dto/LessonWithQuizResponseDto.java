package project.ktc.springboot_app.lesson.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.quiz.dto.QuizQuestionResponseDto;

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
		private String sectionId;
		private Integer orderIndex;
		private LocalDateTime createdAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class QuizWithinLessonResponseDto {
		private List<QuizQuestionResponseDto> questions;
	}
}
