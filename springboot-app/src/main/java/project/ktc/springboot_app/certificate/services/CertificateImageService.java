package project.ktc.springboot_app.certificate.services;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.ktc.springboot_app.certificate.dto.CertificateDataDto;

/**
 * Service for generating image certificates using html-to-image API Converts HTML templates to
 * high-quality image certificates (PNG/JPEG/WebP)
 *
 * <p>Features: - Loads HTML template from resources - Replaces placeholders with dynamic data -
 * Uses external html-to-image API for professional image conversion - Supports multiple image
 * formats (PNG, JPEG, WebP) - Comprehensive error handling and logging - Optimization options for
 * quality and compression
 */
@Service
@Slf4j
public class CertificateImageService {

  private static final String CERTIFICATE_TEMPLATE_PATH =
      "templates/certificates/certificate-background.html";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

  // HTML to Image API endpoint (configurable via application.properties)
  @Value("${certificate.image.api.url:http://localhost:1234/html-to-image}")
  private String imageApiUrl;

  // Default image format (configurable via application.properties)
  @Value("${certificate.image.format:png}")
  private String defaultImageFormat;

  // Default image quality (configurable via application.properties)
  @Value("${certificate.image.quality:95}")
  private int defaultImageQuality;

  private final RestTemplate restTemplate;

  // Constructor to initialize RestTemplate with timeout configurations
  public CertificateImageService() {
    this.restTemplate = new RestTemplate();
    log.info("RestTemplate initialized for html-to-image API calls");
  }

  /**
   * Generate image certificate using html-to-image API
   *
   * @param certificateData Certificate information
   * @return Image as byte array (PNG format by default)
   * @throws IOException if image generation fails
   */
  public byte[] generateCertificateImage(CertificateDataDto certificateData) throws IOException {
    return generateCertificateImage(certificateData, defaultImageFormat);
  }

  /**
   * Generate image certificate with specific format using html-to-image API
   *
   * @param certificateData Certificate information
   * @param format Image format (png, jpeg, webp)
   * @return Image as byte array
   * @throws IOException if image generation fails
   */
  public byte[] generateCertificateImage(CertificateDataDto certificateData, String format)
      throws IOException {
    log.info(
        "Generating {} certificate using html-to-image API for user: {} and course: {}",
        format.toUpperCase(),
        certificateData.getStudentName(),
        certificateData.getCourseTitle());

    try {
      // 1. Load and process HTML template
      String htmlContent = loadAndProcessHtmlTemplate(certificateData);

      // 2. Call html-to-image API
      byte[] imageBytes = callHtmlToImageApi(htmlContent, format);

      log.info(
          "{} certificate generated successfully using API. Size: {} bytes",
          format.toUpperCase(),
          imageBytes.length);
      return imageBytes;

    } catch (Exception e) {
      log.error(
          "Error generating {} certificate with API: {}", format.toUpperCase(), e.getMessage(), e);
      throw new IOException(
          "Failed to generate " + format.toUpperCase() + " certificate: " + e.getMessage(), e);
    }
  }

  /**
   * Alternative method - delegates to main API method Maintained for backward compatibility with
   * PDF service pattern
   *
   * @param certificateData Certificate information
   * @return Image as byte array (PNG format by default)
   * @throws IOException if image generation fails
   */
  public byte[] generateCertificateImageDirect(CertificateDataDto certificateData)
      throws IOException {
    log.info("Using html-to-image API method for certificate generation (direct call)");
    return generateCertificateImage(certificateData);
  }

  /**
   * Call html-to-image API to convert HTML to image
   *
   * @param htmlContent HTML content to convert
   * @param format Target image format (png, jpeg, webp)
   * @return Image as byte array
   * @throws IOException if API call fails
   */
  private byte[] callHtmlToImageApi(String htmlContent, String format) throws IOException {
    try {
      log.info(
          "Calling html-to-image API at: {} with HTML content length: {} characters, format: {}",
          imageApiUrl,
          htmlContent.length(),
          format.toUpperCase());

      // Create request payload
      Map<String, Object> requestBody = createApiRequestBody(htmlContent, format);

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // Set appropriate Accept header based on format
      MediaType acceptType =
          switch (format.toLowerCase()) {
            case "jpeg", "jpg" -> MediaType.IMAGE_JPEG;
            case "webp" -> MediaType.valueOf("image/webp");
            default -> MediaType.IMAGE_PNG; // Default to PNG
          };
      headers.setAccept(Collections.singletonList(acceptType));

      // Create HTTP entity
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

      // Call API with timeout configuration
      ResponseEntity<byte[]> response =
          restTemplate.exchange(imageApiUrl, HttpMethod.POST, requestEntity, byte[].class);

      log.info("html-to-image API responded with status: {}", response.getStatusCode());

      if (response.getStatusCode() == HttpStatus.OK) {
        byte[] imageBytes = response.getBody();
        if (imageBytes != null && imageBytes.length > 0) {
          log.info(
              "{} image generated successfully via API. Size: {} bytes",
              format.toUpperCase(),
              imageBytes.length);
          return imageBytes;
        } else {
          throw new IOException("API returned empty or null image response");
        }
      } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
        throw new IOException("Bad request to image API - invalid HTML or options");
      } else {
        throw new IOException("API returned error status: " + response.getStatusCode());
      }

    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to call html-to-image API: {}", e.getMessage(), e);
      throw new IOException("html-to-image API call failed: " + e.getMessage(), e);
    }
  }

  /**
   * Create request body for html-to-image API
   *
   * @param htmlContent HTML content to convert
   * @param format Target image format (png, jpeg, webp)
   * @return Request body map
   */
  private Map<String, Object> createApiRequestBody(String htmlContent, String format) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("html", htmlContent);

    // Image generation options matching Node.js reference implementation
    Map<String, Object> options = new HashMap<>();

    // Basic options
    options.put("type", format.toLowerCase());
    options.put("width", 1188); // A4 landscape width at 150dpi
    options.put("height", 840); // A4 landscape height at 150dpi
    options.put("omitBackground", false);

    // Quality and optimization settings
    if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
      options.put("quality", defaultImageQuality);
    }

    // WebP specific options
    if ("webp".equalsIgnoreCase(format)) {
      options.put("quality", defaultImageQuality);
      options.put("lossless", false);
    }

    // PNG specific options
    if ("png".equalsIgnoreCase(format)) {
      options.put("optimizeForSize", true);
    }

    // Device scale factor for high-quality rendering
    options.put("deviceScaleFactor", 2);

    // Viewport settings
    Map<String, Object> viewport = new HashMap<>();
    viewport.put("width", 1188);
    viewport.put("height", 840);
    viewport.put("deviceScaleFactor", 2);
    options.put("viewport", viewport);

    requestBody.put("options", options);

    log.debug(
        "Created API request body with HTML length: {} characters, format: {}, and options: {}",
        htmlContent.length(),
        format,
        options);
    return requestBody;
  }

  /**
   * Load HTML template and replace placeholders with actual data
   *
   * @param certificateData Certificate data to populate template
   * @return Processed HTML content with placeholders replaced
   * @throws IOException if template loading fails
   */
  private String loadAndProcessHtmlTemplate(CertificateDataDto certificateData) throws IOException {
    log.debug("Loading and processing certificate HTML template");

    try {
      ClassPathResource resource = new ClassPathResource(CERTIFICATE_TEMPLATE_PATH);

      if (!resource.exists()) {
        throw new IOException("Certificate template not found at: " + CERTIFICATE_TEMPLATE_PATH);
      }

      try (InputStream inputStream = resource.getInputStream()) {
        String htmlContent = new String(inputStream.readAllBytes());

        log.debug("Template loaded successfully, replacing placeholders");

        // Replace placeholders with actual certificate data
        htmlContent =
            htmlContent.replace(
                "[STUDENT_NAME]", escapeHtmlContent(certificateData.getStudentName()));
        htmlContent =
            htmlContent.replace(
                "[COURSE_TITLE]", escapeHtmlContent(certificateData.getCourseTitle()));
        htmlContent =
            htmlContent.replace(
                "[ISSUE_DATE]", DATE_FORMATTER.format(certificateData.getIssueDate()));
        htmlContent =
            htmlContent.replace(
                "[CERTIFICATE_CODE]", escapeHtmlContent(certificateData.getCertificateCode()));

        // Handle instructor name if available
        String instructorName =
            certificateData.getInstructorName() != null
                ? certificateData.getInstructorName()
                : "KTC Faculty";
        htmlContent = htmlContent.replace("[INSTRUCTOR_NAME]", escapeHtmlContent(instructorName));

        // Replace course level if available
        String courseLevel =
            certificateData.getCourseLevel() != null ? certificateData.getCourseLevel() : "General";
        htmlContent = htmlContent.replace("[COURSE_LEVEL]", escapeHtmlContent(courseLevel));

        log.debug("Template processing completed successfully");
        return htmlContent;
      }

    } catch (IOException e) {
      log.error("Error loading or processing certificate template: {}", e.getMessage(), e);
      throw new IOException("Failed to load certificate template: " + e.getMessage(), e);
    }
  }

  /**
   * Escape HTML special characters in content to prevent XSS and formatting issues
   *
   * @param content Content to escape
   * @return HTML-escaped content
   */
  private String escapeHtmlContent(String content) {
    if (content == null) {
      return "";
    }

    return content
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;");
  }

  /**
   * Get image filename for certificate
   *
   * @param userId User ID
   * @param courseId Course ID
   * @param certificateCode Certificate code
   * @param format Image format (png, jpeg, webp)
   * @return Generated filename
   */
  public String generateImageFilename(
      String userId, String courseId, String certificateCode, String format) {
    String extension =
        switch (format.toLowerCase()) {
          case "jpeg", "jpg" -> "jpg";
          case "webp" -> "webp";
          default -> "png";
        };

    return String.format(
        "certificate_%s_%s_%s.%s",
        userId.replaceAll("[^a-zA-Z0-9]", ""),
        courseId.replaceAll("[^a-zA-Z0-9]", ""),
        certificateCode.replaceAll("[^a-zA-Z0-9-]", ""),
        extension);
  }

  /**
   * Get image filename for certificate with default format
   *
   * @param userId User ID
   * @param courseId Course ID
   * @param certificateCode Certificate code
   * @return Generated filename with default format
   */
  public String generateImageFilename(String userId, String courseId, String certificateCode) {
    return generateImageFilename(userId, courseId, certificateCode, defaultImageFormat);
  }

  /** Debug method to test html-to-image API with minimal HTML For testing purposes */
  public byte[] testSimpleImage() throws IOException {
    return testSimpleImage(defaultImageFormat);
  }

  /**
   * Debug method to test html-to-image API with minimal HTML and specific format For testing
   * purposes
   */
  public byte[] testSimpleImage(String format) throws IOException {
    String simpleHtml =
        """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        body {
                            margin: 0;
                            padding: 20px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            font-family: Arial, sans-serif;
                            font-size: 24px;
                            min-height: 100vh;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                        }
                        h1 {
                            font-size: 48px;
                            margin-bottom: 20px;
                            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                        }
                        p {
                            margin: 10px 0;
                            text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
                        }
                    </style>
                </head>
                <body>
                    <h1>TEST CERTIFICATE</h1>
                    <p>This is a test to verify html-to-image API is working.</p>
                    <p>Student: TEST STUDENT</p>
                    <p>Course: TEST COURSE</p>
                    <p>Format: """
            + format.toUpperCase()
            + """
                    </p>
                </body>
                </html>
                """;

    try {
      return callHtmlToImageApi(simpleHtml, format);
    } catch (Exception e) {
      log.error("Test {} image generation failed: {}", format.toUpperCase(), e.getMessage(), e);
      throw new IOException(
          "Test " + format.toUpperCase() + " image generation failed: " + e.getMessage(), e);
    }
  }
}
