package project.ktc.springboot_app.instructor_application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteApplicationDto {
	private String cv;
	private String other;
	private String portfolio;
	private String certificate;
}
