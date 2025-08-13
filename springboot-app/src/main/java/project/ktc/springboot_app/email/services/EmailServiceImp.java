package project.ktc.springboot_app.email.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;
import project.ktc.springboot_app.email.entity.FailedEmail;
import project.ktc.springboot_app.email.interfaces.EmailProvider;
import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.email.repository.FailedEmailRepository;

import java.time.LocalDateTime;
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
    private final FailedEmailRepository failedEmailRepository;
    private final EmailConfig emailConfig;
    private final ObjectMapper objectMapper;

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

    @Override
    @Transactional
    public int retryFailedEmails() {
        log.info("Starting retry process for failed emails");

        List<FailedEmail> failedEmails = failedEmailRepository.findReadyForRetry(LocalDateTime.now());
        log.info("Found {} failed emails ready for retry", failedEmails.size());

        int successCount = 0;

        for (FailedEmail failedEmail : failedEmails) {
            try {
                // Parse the email request from JSON
                EmailRequest emailRequest = objectMapper.readValue(
                        failedEmail.getEmailRequestJson(),
                        EmailRequest.class);

                // Try to send the email
                EmailSendResult result = doSendEmail(emailRequest);

                if (result.isSuccess()) {
                    log.info("Successfully retried failed email ID: {}", failedEmail.getId());
                    failedEmailRepository.delete(failedEmail);
                    successCount++;
                } else {
                    // Update failure count and next retry time
                    failedEmail.incrementRetryCount();
                    failedEmail.setLastError(result.getErrorMessage());
                    failedEmail.setLastAttemptAt(LocalDateTime.now());
                    failedEmail.calculateNextRetryAt(emailConfig.getRetry());

                    if (failedEmail.getRetryCount() >= emailConfig.getRetry().getMaxAttempts()) {
                        log.warn("Failed email ID: {} exceeded max retry attempts", failedEmail.getId());
                        // Keep in database for manual review but don't retry
                    }

                    failedEmailRepository.save(failedEmail);
                }

            } catch (Exception e) {
                log.error("Error retrying failed email ID: {}", failedEmail.getId(), e);

                failedEmail.incrementRetryCount();
                failedEmail.setLastError("Retry error: " + e.getMessage());
                failedEmail.setLastAttemptAt(LocalDateTime.now());
                failedEmail.calculateNextRetryAt(emailConfig.getRetry());

                failedEmailRepository.save(failedEmail);
            }
        }

        log.info("Retry process completed. Successfully sent: {}/{}", successCount, failedEmails.size());
        return successCount;
    }

    @Override
    @Transactional
    public int cleanupFailedEmails(int days) {
        log.info("Cleaning up failed emails older than {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<FailedEmail> oldFailedEmails = failedEmailRepository.findOlderThan(cutoffDate);

        if (!oldFailedEmails.isEmpty()) {
            failedEmailRepository.deleteAll(oldFailedEmails);
            log.info("Cleaned up {} old failed email records", oldFailedEmails.size());
        }

        return oldFailedEmails.size();
    }

    /**
     * Handle sending email after transaction commit
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("emailTaskExecutor")
    public void handleSendAfterCommit(EmailRequest emailRequest) {
        if (emailRequest.isSendAfterCommit()) {
            log.debug("Sending email after transaction commit");
            doSendEmail(emailRequest);
        }
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
        saveFailedEmail(emailRequest, failureResult.getErrorMessage());

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

        saveFailedEmail(emailRequest, failureResult.getErrorMessage());
        return failureResult;
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

    /**
     * Save failed email for retry
     */
    private void saveFailedEmail(EmailRequest emailRequest, String errorMessage) {
        try {
            String emailRequestJson = objectMapper.writeValueAsString(emailRequest);

            FailedEmail failedEmail = FailedEmail.builder()
                    .emailRequestJson(emailRequestJson)
                    .priority(emailRequest.getPriority())
                    .errorMessage(errorMessage)
                    .attemptCount(0)
                    .lastAttemptAt(LocalDateTime.now())
                    .build();

            // Calculate next retry time
            failedEmail.calculateNextRetryAt(emailConfig.getRetry());

            failedEmailRepository.save(failedEmail);

            log.info("Saved failed email for retry. Recipients: {}, Subject: {}",
                    emailRequest.getTo(), emailRequest.getSubject());

        } catch (Exception e) {
            log.error("Failed to save failed email record", e);
        }
    }
}
