package project.ktc.springboot_app.section.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDto {
    private String id;
    private String questionText;
    private Map<String, String> options;
    private String correctAnswer;
    private String explanation;
}
