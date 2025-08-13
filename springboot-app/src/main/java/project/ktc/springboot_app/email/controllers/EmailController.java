package project.ktc.springboot_app.email.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.email.dto.*;
import project.ktc.springboot_app.email.interfaces.EmailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for Email Service
 * Provides endpoints for sending emails with various configurations
 */
@RestController
@RequestMapping("/api/email")
@Tag(name = "Email API", description = "Endpoints for sending emails, managing templates, and email operations")
@Validated
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send email", description = "Send email with custom configuration including attachments, templates, and multiple recipients")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin or Instructor role required"),
            @ApiResponse(responseCode = "500", description = "Email sending failed")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<EmailSendResult>> sendEmail(
            @Valid @RequestBody @Parameter(description = "Email request with all necessary details") EmailRequest emailRequest) {

        log.info("Sending email to: {}, subject: {}", emailRequest.getTo(), emailRequest.getSubject());

        try {
            EmailSendResult result;
            if (emailRequest.isAsync()) {
                CompletableFuture<EmailSendResult> futureResult = emailService.sendEmailAsync(emailRequest);
                result = futureResult.get(); // Wait for completion for API response
            } else {
                result = emailService.sendEmail(emailRequest);
            }

            if (result.isSuccess()) {
                return ApiResponseUtil.success(result, "Email sent successfully");
            } else {
                return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Email sending failed: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed: " + e.getMessage());
        }
    }

    @PostMapping("/send-simple")
    @Operation(summary = "Send simple text email", description = "Send a simple text email to a single recipient")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin or Instructor role required"),
            @ApiResponse(responseCode = "500", description = "Email sending failed")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<EmailSendResult>> sendSimpleEmail(
            @RequestParam @Email(message = "Invalid email format") @NotBlank(message = "Recipient email is required") @Parameter(description = "Recipient email address") String to,

            @RequestParam @NotBlank(message = "Subject is required") @Parameter(description = "Email subject") String subject,

            @RequestParam @NotBlank(message = "Content is required") @Parameter(description = "Email content") String content,

            @RequestParam(defaultValue = "true") @Parameter(description = "Send email asynchronously") boolean async) {

        log.info("Sending simple email to: {}, subject: {}", to, subject);

        try {
            EmailSendResult result;
            if (async) {
                CompletableFuture<EmailSendResult> futureResult = emailService.sendSimpleEmailAsync(to, subject,
                        content);
                result = futureResult.get(); // Wait for completion for API response
            } else {
                result = emailService.sendSimpleEmail(to, subject, content);
            }

            if (result.isSuccess()) {
                return ApiResponseUtil.success(result, "Simple email sent successfully");
            } else {
                return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Email sending failed: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error sending simple email: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed: " + e.getMessage());
        }
    }

    @PostMapping("/send-template")
    @Operation(summary = "Send email with template", description = "Send email using a predefined template with variables")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid template parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin or Instructor role required"),
            @ApiResponse(responseCode = "404", description = "Template not found"),
            @ApiResponse(responseCode = "500", description = "Email sending failed")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<EmailSendResult>> sendTemplateEmail(
            @Valid @RequestBody @Parameter(description = "Template email request") TemplateEmailRequest templateRequest) {

        log.info("Sending template email to: {}, template: {}",
                templateRequest.getTo(), templateRequest.getTemplateName());

        try {
            EmailSendResult result;
            if (templateRequest.isAsync()) {
                CompletableFuture<EmailSendResult> futureResult = emailService.sendTemplateEmailAsync(
                        templateRequest.getTo(),
                        templateRequest.getSubject(),
                        templateRequest.getTemplateName(),
                        templateRequest.getTemplateVariables());
                result = futureResult.get(); // Wait for completion for API response
            } else {
                result = emailService.sendTemplateEmail(
                        templateRequest.getTo(),
                        templateRequest.getSubject(),
                        templateRequest.getTemplateName(),
                        templateRequest.getTemplateVariables());
            }

            if (result.isSuccess()) {
                return ApiResponseUtil.success(result, "Template email sent successfully");
            } else {
                return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Email sending failed: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error sending template email: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed: " + e.getMessage());
        }
    }

    @PostMapping("/send-with-attachment")
    @Operation(summary = "Send email with attachments", description = "Send email with file attachments using multipart form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email with attachments sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email or attachment parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin or Instructor role required"),
            @ApiResponse(responseCode = "413", description = "Attachment too large"),
            @ApiResponse(responseCode = "500", description = "Email sending failed")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<EmailSendResult>> sendEmailWithAttachment(
            @RequestParam @Email(message = "Invalid email format") @NotBlank(message = "Recipient email is required") @Parameter(description = "Recipient email address") String to,

            @RequestParam @NotBlank(message = "Subject is required") @Parameter(description = "Email subject") String subject,

            @RequestParam @NotBlank(message = "Content is required") @Parameter(description = "Email content") String content,

            @RequestParam(required = false) @Parameter(description = "Email attachments") List<MultipartFile> attachments,

            @RequestParam(defaultValue = "true") @Parameter(description = "Send email asynchronously") boolean async) {

        log.info("Sending email with attachments to: {}, subject: {}", to, subject);

        try {
            // Build email request with attachments
            EmailRequest.EmailRequestBuilder requestBuilder = EmailRequest.builder()
                    .to(List.of(to))
                    .subject(subject)
                    .htmlBody(content)
                    .async(async);

            // Add attachments if provided
            if (attachments != null && !attachments.isEmpty()) {
                List<EmailAttachment> emailAttachments = attachments.stream()
                        .map(file -> EmailAttachment.builder()
                                .filename(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .content(getFileBytes(file))
                                .build())
                        .toList();
                requestBuilder.attachments(emailAttachments);
            }

            EmailRequest emailRequest = requestBuilder.build();

            EmailSendResult result;
            if (async) {
                CompletableFuture<EmailSendResult> futureResult = emailService.sendEmailAsync(emailRequest);
                result = futureResult.get(); // Wait for completion for API response
            } else {
                result = emailService.sendEmail(emailRequest);
            }

            if (result.isSuccess()) {
                return ApiResponseUtil.success(result, "Email with attachments sent successfully");
            } else {
                return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Email sending failed: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Error sending email with attachments: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Email sending failed: " + e.getMessage());
        }
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed emails", description = "Retry sending failed emails from the queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failed emails retry completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Failed email retry operation failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Integer>>> retryFailedEmails() {

        log.info("Retrying failed emails");

        try {
            int processedCount = emailService.retryFailedEmails();

            Map<String, Integer> result = new HashMap<>();
            result.put("processedCount", processedCount);

            return ApiResponseUtil.success(result,
                    String.format("Processed %d failed emails for retry", processedCount));

        } catch (Exception e) {
            log.error("Error retrying failed emails: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed email retry operation failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/cleanup-failed")
    @Operation(summary = "Cleanup old failed emails", description = "Remove old failed email records from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failed emails cleanup completed"),
            @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Cleanup operation failed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Integer>>> cleanupFailedEmails(
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "Days must be at least 1") @Parameter(description = "Number of days to keep failed email records") int days) {

        log.info("Cleaning up failed emails older than {} days", days);

        try {
            int cleanedCount = emailService.cleanupFailedEmails(days);

            Map<String, Integer> result = new HashMap<>();
            result.put("cleanedCount", cleanedCount);
            result.put("daysThreshold", days);

            return ApiResponseUtil.success(result,
                    String.format("Cleaned up %d failed email records older than %d days", cleanedCount, days));

        } catch (Exception e) {
            log.error("Error cleaning up failed emails: {}", e.getMessage(), e);
            return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cleanup operation failed: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Email service health check", description = "Check if email service is running and configured properly")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email service is healthy"),
            @ApiResponse(responseCode = "500", description = "Email service is not healthy")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Object>>> healthCheck() {

        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "Email Service");
            health.put("timestamp", System.currentTimeMillis());

            // Could add more health checks here like:
            // - SMTP connection test
            // - Template availability check
            // - Queue status check

            return ApiResponseUtil.success(health, "Email service is healthy");

        } catch (Exception e) {
            log.error("Email service health check failed: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Email service is not healthy");
        }
    }

    /**
     * Helper method to safely extract bytes from MultipartFile
     */
    private byte[] getFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            log.error("Error reading file bytes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
        }
    }
}
