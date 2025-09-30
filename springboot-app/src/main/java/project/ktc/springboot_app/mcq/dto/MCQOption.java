package project.ktc.springboot_app.mcq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCQOption {
	private String question;
	private String answer;
	private List<String> options;

	@JsonProperty("correct_index")
	private int correctIndex;

	private Map<String, Object> metadata;
}
