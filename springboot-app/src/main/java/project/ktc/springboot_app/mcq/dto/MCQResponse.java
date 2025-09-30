package project.ktc.springboot_app.mcq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCQResponse {
	private String status;
	private String file;

	@JsonProperty("total_questions")
	private int totalQuestions;

	private List<MCQOption> mcqs;
}
