package project.ktc.springboot_app.mcq.services;

import java.io.File;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import project.ktc.springboot_app.mcq.dto.MCQResponse;

@Service
public class MCQService {

  private final String FASTAPI_URL = "http://localhost:8000/api/mcq/generate";
  private final RestTemplate restTemplate = new RestTemplate();

  public MCQResponse generateMCQsFromFile(File file, int numQuestions) {
    // Chuẩn bị request body
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new FileSystemResource(file));
    body.add("num_questions", numQuestions);

    // Chuẩn bị headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Tạo request entity
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    try {
      // Gọi FastAPI
      ResponseEntity<MCQResponse> response =
          restTemplate.postForEntity(FASTAPI_URL, requestEntity, MCQResponse.class);

      if (response.getStatusCode() == HttpStatus.OK
          && "success".equals(response.getBody().getStatus())) {
        return response.getBody();
      } else {
        throw new RuntimeException("API returned error: " + response.getStatusCode());
      }

    } catch (Exception e) {
      throw new RuntimeException("Error calling FastAPI: " + e.getMessage(), e);
    }
  }
}
