package project.ktc.springboot_app.certificate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.ktc.springboot_app.certificate.dto.CertificateDataDto;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating PDF certificates using HTML-to-PDF API
 * Converts HTML templates to high-quality PDF certificates
 * 
 * Features:
 * - Loads HTML template from resources
 * - Replaces placeholders with dynamic data
 * - Uses external HTML-to-PDF API for professional PDF conversion
 * - Comprehensive error handling and logging
 */
@Service
@Slf4j
public class CertificatePdfService {

    private static final String CERTIFICATE_TEMPLATE_PATH = "templates/certificates/certificate-background.html";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    // HTML to PDF API endpoint (configurable via application.properties)
    @Value("${certificate.pdf.api.url:http://localhost:1234/html-to-pdf}")
    private String pdfApiUrl;

    private final RestTemplate restTemplate;

    // Constructor to initialize RestTemplate with timeout configurations
    public CertificatePdfService() {
        this.restTemplate = new RestTemplate();
        log.info("RestTemplate initialized for HTML-to-PDF API calls");
    }

    /**
     * Generate PDF certificate using HTML-to-PDF API
     * 
     * @param certificateData Certificate information
     * @return PDF as byte array
     * @throws IOException if PDF generation fails
     */
    public byte[] generateCertificatePdf(CertificateDataDto certificateData) throws IOException {
        log.info("Generating PDF certificate using HTML-to-PDF API for user: {} and course: {}",
                certificateData.getStudentName(), certificateData.getCourseTitle());

        try {
            // 1. Load and process HTML template
            String htmlContent = loadAndProcessHtmlTemplate(certificateData);

            // 2. Call HTML-to-PDF API
            byte[] pdfBytes = callHtmlToPdfApi(htmlContent);

            log.info("PDF certificate generated successfully using API. Size: {} bytes", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Error generating PDF certificate with API: {}", e.getMessage(), e);
            throw new IOException("Failed to generate PDF certificate: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative method - delegates to main API method
     * Maintained for backward compatibility
     * 
     * @param certificateData Certificate information
     * @return PDF as byte array
     * @throws IOException if PDF generation fails
     */
    public byte[] generateCertificatePdfDirect(CertificateDataDto certificateData) throws IOException {
        log.info("Using HTML-to-PDF API method for certificate generation (direct call)");
        return generateCertificatePdf(certificateData);
    }

    /**
     * Call HTML-to-PDF API to convert HTML to PDF
     * 
     * @param htmlContent HTML content to convert
     * @return PDF as byte array
     * @throws IOException if API call fails
     */
    private byte[] callHtmlToPdfApi(String htmlContent) throws IOException {
        try {
            log.info("Calling HTML-to-PDF API at: {} with HTML content length: {} characters",
                    pdfApiUrl, htmlContent.length());

            // Create request payload
            Map<String, Object> requestBody = createApiRequestBody(htmlContent);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));

            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Call API with timeout configuration
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    pdfApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class);

            log.info("HTML-to-PDF API responded with status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] pdfBytes = response.getBody();
                if (pdfBytes != null && pdfBytes.length > 0) {
                    log.info("PDF generated successfully via API. Size: {} bytes", pdfBytes.length);
                    return pdfBytes;
                } else {
                    throw new IOException("API returned empty or null PDF response");
                }
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new IOException("Bad request to PDF API - invalid HTML or options");
            } else {
                throw new IOException("API returned error status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call HTML-to-PDF API: {}", e.getMessage(), e);
            throw new IOException("HTML-to-PDF API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create request body for HTML-to-PDF API
     * 
     * @param htmlContent HTML content to convert
     * @return Request body map
     */
    private Map<String, Object> createApiRequestBody(String htmlContent) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("html", htmlContent);

        // PDF generation options matching API specification
        Map<String, Object> options = new HashMap<>();
        options.put("format", "A4");
        options.put("landscape", true);
        options.put("printBackground", true);
        options.put("scale", 0.8);
        options.put("displayHeaderFooter", false);

        // Zero margins for full-page certificate
        Map<String, String> margin = new HashMap<>();
        margin.put("top", "0mm");
        margin.put("right", "0mm");
        margin.put("bottom", "0mm");
        margin.put("left", "0mm");
        options.put("margin", margin);

        // Optional: specify custom width/height if needed
        // options.put("width", "297mm");
        // options.put("height", "210mm");

        requestBody.put("options", options);

        log.debug("Created API request body with HTML length: {} characters and options: {}",
                htmlContent.length(), options);
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
                htmlContent = htmlContent.replace("[STUDENT_NAME]",
                        escapeHtmlContent(certificateData.getStudentName()));
                htmlContent = htmlContent.replace("[COURSE_TITLE]",
                        escapeHtmlContent(certificateData.getCourseTitle()));
                htmlContent = htmlContent.replace("[ISSUE_DATE]",
                        DATE_FORMATTER.format(certificateData.getIssueDate()));
                htmlContent = htmlContent.replace("[CERTIFICATE_CODE]",
                        escapeHtmlContent(certificateData.getCertificateCode()));

                // Handle instructor name if available
                String instructorName = certificateData.getInstructorName() != null
                        ? certificateData.getInstructorName()
                        : "KTC Faculty";
                htmlContent = htmlContent.replace("[INSTRUCTOR_NAME]",
                        escapeHtmlContent(instructorName));

                // Replace course level if available
                String courseLevel = certificateData.getCourseLevel() != null
                        ? certificateData.getCourseLevel()
                        : "General";
                htmlContent = htmlContent.replace("[COURSE_LEVEL]",
                        escapeHtmlContent(courseLevel));

                log.debug("Template processing completed successfully");
                return htmlContent;
            }

        } catch (IOException e) {
            log.error("Error loading or processing certificate template: {}", e.getMessage(), e);
            throw new IOException("Failed to load certificate template: " + e.getMessage(), e);
        }
    }

    /**
     * Escape HTML special characters in content to prevent XSS and formatting
     * issues
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
     * Get PDF filename for certificate
     */
    public String generatePdfFilename(String userId, String courseId, String certificateCode) {
        return String.format("certificate_%s_%s_%s.pdf",
                userId.replaceAll("[^a-zA-Z0-9]", ""),
                courseId.replaceAll("[^a-zA-Z0-9]", ""),
                certificateCode.replaceAll("[^a-zA-Z0-9-]", ""));
    }

    /**
     * Debug method to test HTML-to-PDF API with minimal HTML
     * For testing purposes
     */
    public byte[] testSimplePdf() throws IOException {
        String simpleHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        @page { size: A4 landscape; margin: 0; }
                        body {
                            margin: 0;
                            padding: 20px;
                            background: red;
                            color: white;
                            font-size: 24px;
                        }
                    </style>
                </head>
                <body>
                    <h1>TEST CERTIFICATE</h1>
                    <p>This is a test to verify HTML-to-PDF API is working.</p>
                    <p>Student: TEST STUDENT</p>
                    <p>Course: TEST COURSE</p>
                </body>
                </html>
                """;

        try {
            return callHtmlToPdfApi(simpleHtml);
        } catch (Exception e) {
            log.error("Test PDF generation failed: {}", e.getMessage(), e);
            throw new IOException("Test PDF generation failed: " + e.getMessage(), e);
        }
    }
}
