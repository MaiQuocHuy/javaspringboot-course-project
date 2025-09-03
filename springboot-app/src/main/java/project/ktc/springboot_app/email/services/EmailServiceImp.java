package project.ktc.springboot_app.email.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;
import project.ktc.springboot_app.email.interfaces.EmailProvider;
import project.ktc.springboot_app.email.interfaces.EmailService;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main email service implementation
 * Handles email sending with retry logic and provider fallback
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

    private final List<EmailProvider> emailProviders;
    private final EmailConfig emailConfig;

    @Override
    public EmailSendResult sendEmail(EmailRequest emailRequest) {
        log.info("Sending email to: {}, subject: {}", emailRequest.getTo(), emailRequest.getSubject());

        // Check if should send after transaction commit
        if (emailRequest.isSendAfterCommit()) {
            log.debug("Email will be sent after transaction commit");
            // This will be handled by transaction event listener
            return EmailSendResult.success("Email scheduled for sending after transaction commit", "pending");
        }

        return doSendEmail(emailRequest);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<EmailSendResult> sendEmailAsync(EmailRequest emailRequest) {
        log.info("Sending email asynchronously to: {}, subject: {}", emailRequest.getTo(), emailRequest.getSubject());

        try {
            EmailSendResult result = sendEmail(emailRequest);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Async email sending failed", e);
            return CompletableFuture.completedFuture(
                    EmailSendResult.failure("Async email sending failed: " + e.getMessage(), "unknown"));
        }
    }

    @Override
    public EmailSendResult sendTemplateEmail(String to, String subject, String templateName,
            Map<String, Object> templateVariables) {
        EmailRequest request = EmailRequest.builder()
                .to(List.of(to))
                .subject(subject)
                .templateName(templateName)
                .templateVariables(templateVariables != null ? templateVariables : new HashMap<>())
                .async(false)
                .build();

        return sendEmail(request);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<EmailSendResult> sendTemplateEmailAsync(String to, String subject,
            String templateName,
            Map<String, Object> templateVariables) {
        EmailRequest request = EmailRequest.builder()
                .to(List.of(to))
                .subject(subject)
                .templateName(templateName)
                .templateVariables(templateVariables != null ? templateVariables : new HashMap<>())
                .async(true)
                .build();

        return sendEmailAsync(request);
    }

    @Override
    public EmailSendResult sendSimpleEmail(String to, String subject, String content) {
        EmailRequest request = EmailRequest.builder()
                .to(List.of(to))
                .subject(subject)
                .htmlBody(content)
                .async(false)
                .build();

        return sendEmail(request);
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<EmailSendResult> sendSimpleEmailAsync(String to, String subject, String content) {
        EmailRequest request = EmailRequest.builder()
                .to(List.of(to))
                .subject(subject)
                .htmlBody(content)
                .async(true)
                .build();

        return sendEmailAsync(request);
    }

    /**
     * Actually send the email using available providers
     */
    private EmailSendResult doSendEmail(EmailRequest emailRequest) {
        // Get primary provider
        EmailProvider primaryProvider = getProviderByName(emailConfig.getActiveProvider());

        if (primaryProvider != null && primaryProvider.isAvailable()) {
            try {
                EmailSendResult result = primaryProvider.sendEmail(emailRequest);

                if (result.isSuccess()) {
                    log.debug("Email sent successfully using provider: {}", primaryProvider.getProviderName());
                    return result;
                }

                log.warn("Primary provider {} failed: {}", primaryProvider.getProviderName(), result.getErrorMessage());

                // Try fallback if enabled
                if (emailConfig.getProvider().isEnableFallback()) {
                    return tryFallbackProvider(emailRequest, primaryProvider.getProviderName());
                }

            } catch (Exception e) {
                log.error("Primary provider {} threw exception", primaryProvider.getProviderName(), e);

                if (emailConfig.getProvider().isEnableFallback()) {
                    return tryFallbackProvider(emailRequest, primaryProvider.getProviderName());
                }
            }
        }

        // If we reach here, all providers failed
        EmailSendResult failureResult = EmailSendResult.failure(
                "All email providers failed",
                "unknown");

        // Save to failed emails for retry

        return failureResult;
    }

    /**
     * Try fallback provider
     */
    private EmailSendResult tryFallbackProvider(EmailRequest emailRequest, String excludeProvider) {
        String fallbackProviderName = emailConfig.getFallbackProvider();

        if (fallbackProviderName != null && !fallbackProviderName.equals(excludeProvider)) {
            EmailProvider fallbackProvider = getProviderByName(fallbackProviderName);

            if (fallbackProvider != null && fallbackProvider.isAvailable()) {
                try {
                    log.info("Trying fallback provider: {}", fallbackProvider.getProviderName());
                    EmailSendResult result = fallbackProvider.sendEmail(emailRequest);

                    if (result.isSuccess()) {
                        log.info("Email sent successfully using fallback provider: {}",
                                fallbackProvider.getProviderName());
                        return result;
                    }

                    log.warn("Fallback provider {} also failed: {}",
                            fallbackProvider.getProviderName(), result.getErrorMessage());

                } catch (Exception e) {
                    log.error("Fallback provider {} threw exception", fallbackProvider.getProviderName(), e);
                }
            }
        }

        // Save to failed emails for retry
        EmailSendResult failureResult = EmailSendResult.failure(
                "Both primary and fallback providers failed",
                "unknown");

        return failureResult;
    }

    /**
     * Send payment confirmation email asynchronously
     */
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<EmailSendResult> sendPaymentConfirmationEmailAsync(
            String customerEmail,
            String customerName,
            String courseTitle,
            String courseUrl,
            String instructorName,
            String courseLevel,
            String courseDuration,
            String lessonCount,
            String amount,
            String transactionId,
            String paymentMethod,
            java.time.LocalDateTime paymentDate) {

        try {
            // Create template variables
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("customerName", customerName);
            templateVariables.put("courseTitle", courseTitle);
            templateVariables.put("courseUrl", courseUrl);
            templateVariables.put("instructorName", instructorName);
            templateVariables.put("courseLevel", courseLevel);
            templateVariables.put("courseDuration", courseDuration);
            templateVariables.put("lessonCount", lessonCount);
            templateVariables.put("amount", amount);
            templateVariables.put("transactionId", transactionId);
            templateVariables.put("paymentMethod", paymentMethod);
            // templateVariables.put("paymentDate",
            // paymentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            templateVariables.put("paymentDateFormatted",
                    paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            templateVariables.put("year", String.valueOf(Year.now().getValue()));

            // Create email request
            EmailRequest request = EmailRequest.builder()
                    .to(List.of(customerEmail))
                    .subject("Payment Confirmation - Welcome to " + courseTitle)
                    .templateName("payment-confirmation-template")
                    .templateVariables(templateVariables)
                    .async(true)
                    .build();

            EmailSendResult result = sendEmail(request);
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {}: {}", customerEmail, e.getMessage(), e);
            EmailSendResult errorResult = EmailSendResult.builder()
                    .success(false)
                    .errorMessage("Failed to send payment confirmation email: " + e.getMessage())
                    .build();
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    /**
     * Send certificate notification email asynchronously
     */
    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<EmailSendResult> sendCertificateNotificationEmailAsync(
            String studentEmail,
            String studentName,
            String courseTitle,
            String instructorName,
            String certificateCode,
            String certificateUrl,
            java.time.LocalDateTime issueDate) {

        try {
            log.info("Sending certificate notification email to: {}", studentEmail);

            // Create template variables
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("studentName", studentName);
            templateVariables.put("courseTitle", courseTitle);
            templateVariables.put("instructorName", instructorName);
            templateVariables.put("certificateCode", certificateCode);
            templateVariables.put("certificateUrl", certificateUrl);
            templateVariables.put("issueDate", issueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            templateVariables.put("year", String.valueOf(Year.now().getValue()));

            // Create verification URL
            String verificationUrl = String.format("https://certificates.ktc.edu/verify/%s", certificateCode);
            templateVariables.put("verificationUrl", verificationUrl);

            // Create email request
            EmailRequest request = EmailRequest.builder()
                    .to(List.of(studentEmail))
                    .subject("🎓 Your Certificate is Ready - " + courseTitle)
                    .templateName("certificate-notification-template")
                    .templateVariables(templateVariables)
                    .async(true)
                    .build();

            EmailSendResult result = sendEmail(request);

            if (result.isSuccess()) {
                log.info("Certificate notification email sent successfully to: {}", studentEmail);
            } else {
                log.error("Failed to send certificate notification email to {}: {}", studentEmail,
                        result.getErrorMessage());
            }

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Failed to send certificate notification email to {}: {}", studentEmail, e.getMessage(), e);
            EmailSendResult errorResult = EmailSendResult.builder()
                    .success(false)
                    .errorMessage("Failed to send certificate notification email: " + e.getMessage())
                    .build();
            return CompletableFuture.completedFuture(errorResult);
        }
    }

    /**
     * Get provider by name
     */
    private EmailProvider getProviderByName(String providerName) {
        return emailProviders.stream()
                .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElse(null);
    }

}
