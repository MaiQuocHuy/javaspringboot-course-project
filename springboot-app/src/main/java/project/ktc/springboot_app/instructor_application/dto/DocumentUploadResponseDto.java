package project.ktc.springboot_app.instructor_application.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for document upload response */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponseDto {

  private String userId;
  private Map<String, String> documents;
}
