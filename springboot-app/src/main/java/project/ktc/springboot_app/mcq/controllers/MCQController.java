package project.ktc.springboot_app.mcq.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.mcq.dto.MCQResponse;
import project.ktc.springboot_app.mcq.services.MCQService;

@RestController
@RequestMapping("/api/mcq")
@PreAuthorize("hasRole('INSTRUCTOR')")
@Tag(
    name = "MCQ Generation API",
    description =
        "Endpoints for generating Multiple Choice Questions (MCQs) from uploaded files. Requires INSTRUCTOR role.")
@Validated
public class MCQController {

  @Autowired private MCQService mcqService;

  private static final String UPLOAD_DIR = "uploads/";

  // private final String MCQ_API_URL = "http://localhost:8000";

  // @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  // @Operation(summary = "Generate MCQs from file", description = "Generates MCQs
  // from a provided file and number of questions")
  // @ApiResponses(value = {
  // @ApiResponse(responseCode = "200", description = "MCQs generated
  // successfully"),
  // @ApiResponse(responseCode = "400", description = "Invalid file or
  // parameters"),
  // @ApiResponse(responseCode = "500", description = "Internal server error")
  // })
  // public
  // ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<MCQResponse>>
  // generateMCQFromFile(
  // @Parameter(description = "Optional course thumbnail image file", required =
  // false) @RequestPart(value = "file", required = false) MultipartFile file,
  // @Parameter(description = "Number of questions to generate", required = true)
  // @RequestParam("num_questions") int numQuestions) {
  // try {
  // // Prepare headers
  // HttpHeaders headers = new HttpHeaders();
  // headers.setContentType(MediaType.MULTIPART_FORM_DATA);

  // // Prepare request body
  // MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
  // body.add("file", file.getResource());
  // body.add("num_questions", numQuestions);

  // HttpEntity<MultiValueMap<String, Object>> requestEntity = new
  // HttpEntity<>(body, headers);

  // // Make POST request
  // ResponseEntity<MCQResponse> response = restTemplate.exchange(
  // MCQ_API_URL + "/upload",
  // HttpMethod.POST,
  // requestEntity,
  // MCQResponse.class // 👈 deserialize trực tiếp
  // );

  // // Wrap in custom ApiResponse
  // return ApiResponseUtil.success(response.getBody(), "MCQs generated
  // successfully");

  // } catch (Exception e) {
  // return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
  // "Failed to generate MCQs: " + e.getMessage());
  // }
  // }

  @PostMapping(
      value = "/generate",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Generate MCQs from uploaded file",
      description =
          "Upload a text or DOCX file to generate Multiple Choice Questions (MCQs). The system will process the file content and generate the specified number of questions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "MCQs generated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MCQResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format or parameters. Supported formats: TXT, DOCX",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - INSTRUCTOR role required",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during MCQ generation",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<MCQResponse> generateMCQs(
      @Parameter(
              description = "File to upload for MCQ generation. Supported formats: .txt, .docx",
              required = true,
              schema = @Schema(type = "string", format = "binary"))
          @RequestPart("file")
          MultipartFile file,
      @Parameter(
              description = "Number of MCQ questions to generate from the file content",
              required = true,
              example = "10",
              schema = @Schema(type = "integer", minimum = "1", maximum = "50"))
          @RequestParam("num_questions")
          @Min(value = 1, message = "Number of questions must be at least 1")
          @Max(value = 50, message = "Number of questions cannot exceed 50")
          int numQuestions) {
    try {
      // Validate file
      if (file.isEmpty()) {
        return ResponseEntity.badRequest()
            .body(createErrorResponse("File is empty", file.getOriginalFilename(), 0));
      }

      if (numQuestions < 1 || numQuestions > 50) {
        return ResponseEntity.badRequest()
            .body(
                createErrorResponse(
                    "Number of questions must be between 1 and 50", file.getOriginalFilename(), 0));
      }

      // Validate file type
      String filename = file.getOriginalFilename();
      if (filename == null
          || (!filename.toLowerCase().endsWith(".txt")
              && !filename.toLowerCase().endsWith(".docx"))) {
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Only TXT and DOCX files are supported", filename, 0));
      }

      // Tạo thư mục upload nếu chưa có
      Path uploadPath = Paths.get(UPLOAD_DIR);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // Lưu file tạm với timestamp để tránh conflict
      String timestamp = String.valueOf(System.currentTimeMillis());
      File tempFile = new File(UPLOAD_DIR + timestamp + "_" + filename);
      Files.write(tempFile.toPath(), file.getBytes());

      try {
        // Gọi FastAPI
        MCQResponse result = mcqService.generateMCQsFromFile(tempFile, numQuestions);
        return ResponseEntity.ok(result);

      } finally {
        // Xoá file tạm trong finally block để đảm bảo cleanup
        if (tempFile.exists()) {
          tempFile.delete();
        }
      }

    } catch (IOException e) {
      return ResponseEntity.status(500)
          .body(
              createErrorResponse(
                  "Error processing file: " + e.getMessage(), file.getOriginalFilename(), 0));
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body(
              createErrorResponse(
                  "Internal server error: " + e.getMessage(), file.getOriginalFilename(), 0));
    }
  }

  /** Helper method to create standardized error response */
  private MCQResponse createErrorResponse(
      String errorMessage, String filename, int totalQuestions) {
    MCQResponse errorResponse = new MCQResponse();
    errorResponse.setStatus("error");
    errorResponse.setFile(filename);
    errorResponse.setTotalQuestions(totalQuestions);
    // Assuming MCQResponse has a setMessage method or similar field for error
    // details
    return errorResponse;
  }
}
