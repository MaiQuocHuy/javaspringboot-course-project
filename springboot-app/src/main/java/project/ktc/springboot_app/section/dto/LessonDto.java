package project.ktc.springboot_app.section.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
	private String id;
	private String title;
	private String type;
	private Integer order;
	private VideoDto video;
	private QuizDto quiz;
	private Boolean isCompleted;
	private LocalDateTime completedAt;
}
