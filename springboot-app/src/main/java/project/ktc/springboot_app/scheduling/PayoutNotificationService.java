package project.ktc.springboot_app.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.config.PayoutSchedulingProperties;
import project.ktc.springboot_app.email.interfaces.EmailService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification service for automated payout processing
 * Sends email notifications about payout events to administrators
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.payout.notification.enabled", havingValue = "true", matchIfMissing = true)
public class PayoutNotificationService {

    private final EmailService emailService;
    private final PayoutSchedulingProperties payoutProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Send notification about payout processing summary
     * 
     * @param successCount Number of successful payouts
     * @param failureCount Number of failed payouts
     * @param totalAmount Total amount processed
     */
    public void sendPayoutProcessingSummary(int successCount, int failureCount, BigDecimal totalAmount) {
        if (!payoutProperties.getNotification().isEnabled()) {
            return;
        }

        log.info("📧 Sending payout processing summary notification");

        try {
            String subject = String.format("Automatic Payout Processing Summary - %s", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            String content = buildPayoutSummaryContent(successCount, failureCount, totalAmount);

            sendNotificationToAdmins(subject, content);

        } catch (Exception e) {
            log.error("Error sending payout processing summary notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification about payout processing error
     * 
     * @param error The error that occurred
     */
    public void sendPayoutProcessingError(Exception error) {
        if (!payoutProperties.getNotification().isEnabled()) {
            return;
        }

        log.info("🚨 Sending payout processing error notification");

        try {
            String subject = "⚠️ Automatic Payout Processing Error - " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            String content = buildErrorNotificationContent(error);

            sendNotificationToAdmins(subject, content);

        } catch (Exception e) {
            log.error("Error sending payout processing error notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send daily summary notification
     * 
     * @param summary The payout eligibility summary
     */
    public void sendDailySummary(PayoutEligibilityService.PayoutEligibilitySummary summary) {
        if (!payoutProperties.getNotification().isEnabled()) {
            return;
        }

        log.info("📋 Sending daily payout summary notification");

        try {
            String subject = String.format("Daily Payout Summary - %s", 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            String content = buildDailySummaryContent(summary);

            sendNotificationToAdmins(subject, content);

        } catch (Exception e) {
            log.error("Error sending daily payout summary notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification to all configured admin emails
     * 
     * @param subject Email subject
     * @param content Email content
     */
    private void sendNotificationToAdmins(String subject, String content) {
        List<String> adminEmails = payoutProperties.getNotification().getAdmin().getEmails();
        
        log.info("📧 Attempting to send notification to {} admin emails: {}", adminEmails.size(), adminEmails);
        
        for (String adminEmail : adminEmails) {
            try {
                log.info("📤 Sending payout notification to admin: {}", adminEmail);
                
                // Try using simple email first (without template)
                var result = emailService.sendSimpleEmail(adminEmail, subject, content);
                
                if (result.isSuccess()) {
                    log.info("✅ Successfully sent payout notification to admin: {}", adminEmail);
                } else {
                    log.error("❌ Failed to send notification to {}: {}", adminEmail, result.getErrorMessage());
                    
                    // Try with template as fallback
                    tryWithTemplate(adminEmail, subject, content);
                }

            } catch (Exception e) {
                log.error("❌ Exception sending payout notification to admin {}: {}", adminEmail, e.getMessage(), e);
                
                // Try with template as fallback
                tryWithTemplate(adminEmail, subject, content);
            }
        }
    }
    
    /**
     * Fallback method to try sending with template
     */
    private void tryWithTemplate(String adminEmail, String subject, String content) {
        try {
            log.info("🔄 Trying template email for admin: {}", adminEmail);
            
            var result = emailService.sendTemplateEmail(adminEmail, subject, "general-template", createTemplateData(subject, content));
            
            if (result.isSuccess()) {
                log.info("✅ Successfully sent template notification to admin: {}", adminEmail);
            } else {
                log.error("❌ Template email also failed for {}: {}", adminEmail, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("❌ Template email exception for admin {}: {}", adminEmail, e.getMessage(), e);
        }
    }

    /**
     * Create template data for email notifications
     */
    private Map<String, Object> createTemplateData(String subject, String content) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("title", subject);
        templateData.put("content", content);
        templateData.put("timestamp", LocalDateTime.now().format(FORMATTER));
        templateData.put("platform", "KTC Learning Platform");
        return templateData;
    }

    /**
     * Build content for payout processing summary
     */
    private String buildPayoutSummaryContent(int successCount, int failureCount, BigDecimal totalAmount) {
        StringBuilder content = new StringBuilder();
        ZonedDateTime nowVN = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        content.append("<h3>🎯 Automatic Payout Processing Summary</h3>");
        content.append("<div style='margin: 20px 0;'>");
        content.append("<p><strong>Processing Time:</strong> ")
                .append(nowVN.format(FORMATTER))
                .append("</p>");
        content.append("<p><strong>✅ Successful Payouts:</strong> ").append(successCount).append("</p>");
        content.append("<p><strong>❌ Failed Payouts:</strong> ").append(failureCount).append("</p>");
        content.append("<p><strong>💰 Total Amount Processed:</strong> $").append(totalAmount).append("</p>");
        content.append("</div>");

        if (successCount > 0) {
            content.append("<p>✅ Instructor earnings have been created and are available for payout.</p>");
        }

        if (failureCount > 0) {
            content.append("<p>⚠️ Some payouts failed and may require manual review.</p>");
        }

        content.append("<hr>");
        content.append("<p><em>This is an automated notification from the KTC Learning Platform payout system.</em></p>");

        return content.toString();
    }

    /**
     * Build content for error notification
     */
    private String buildErrorNotificationContent(Exception error) {
        StringBuilder content = new StringBuilder();
        content.append("<h3>🚨 Automatic Payout Processing Error</h3>");
        content.append("<div style='margin: 20px 0; padding: 10px; background-color: #ffebee; border-left: 4px solid #f44336;'>");
        content.append("<p><strong>Error Time:</strong> ").append(LocalDateTime.now().format(FORMATTER)).append("</p>");
        content.append("<p><strong>Error Type:</strong> ").append(error.getClass().getSimpleName()).append("</p>");
        content.append("<p><strong>Error Message:</strong> ").append(error.getMessage()).append("</p>");
        content.append("</div>");

        content.append("<h4>⚠️ Action Required</h4>");
        content.append("<p>The automatic payout processing encountered an error and requires immediate attention:</p>");
        content.append("<ul>");
        content.append("<li>Check the application logs for detailed error information</li>");
        content.append("<li>Verify database connectivity and system health</li>");
        content.append("<li>Review pending payments that may need manual processing</li>");
        content.append("<li>Contact the development team if the issue persists</li>");
        content.append("</ul>");

        content.append("<hr>");
        content.append("<p><em>This is an automated error notification from the KTC Learning Platform payout system.</em></p>");

        return content.toString();
    }

    /**
     * Build content for daily summary
     */
    private String buildDailySummaryContent(PayoutEligibilityService.PayoutEligibilitySummary summary) {
        StringBuilder content = new StringBuilder();
        content.append("<h3>📊 Daily Payout Summary</h3>");
        content.append("<div style='margin: 20px 0;'>");
        content.append("<p><strong>Report Date:</strong> ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("</p>");
        content.append("</div>");

        content.append("<table style='border-collapse: collapse; width: 100%; margin: 20px 0;'>");
        content.append("<tr style='background-color: #f5f5f5;'>");
        content.append("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Metric</th>");
        content.append("<th style='border: 1px solid #ddd; padding: 8px; text-align: right;'>Count</th>");
        content.append("</tr>");

        content.append("<tr>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px;'>Total Completed Payments</td>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(summary.getTotalCompletedPayments()).append("</td>");
        content.append("</tr>");

        content.append("<tr style='background-color: #f9f9f9;'>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px;'>Already Paid Out</td>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(summary.getAlreadyPaidOut()).append("</td>");
        content.append("</tr>");

        content.append("<tr>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px;'>Within Waiting Period</td>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(summary.getWithinWaitingPeriod()).append("</td>");
        content.append("</tr>");

        content.append("<tr style='background-color: #f9f9f9;'>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px;'>With Blocking Refunds</td>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>").append(summary.getWithBlockingRefunds()).append("</td>");
        content.append("</tr>");

        content.append("<tr style='background-color: #e8f5e8;'>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px;'><strong>Eligible for Payout</strong></td>");
        content.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'><strong>").append(summary.getEligibleForPayout()).append("</strong></td>");
        content.append("</tr>");

        content.append("</table>");

        if (summary.getEligibleForPayout() > 0) {
            content.append("<div style='margin: 20px 0; padding: 10px; background-color: #e8f5e8; border-left: 4px solid #4caf50;'>");
            content.append("<p>✅ <strong>").append(summary.getEligibleForPayout()).append("</strong> payments are ready for automatic processing.</p>");
            content.append("</div>");
        }

        content.append("<hr>");
        content.append("<p><em>This is an automated daily summary from the KTC Learning Platform payout system.</em></p>");

        return content.toString();
    }
    
    /**
     * Send test notification to verify email configuration
     * This method can be called manually to test email sending
     */
    public void sendTestNotification() {
        if (!payoutProperties.getNotification().isEnabled()) {
            log.warn("⚠️ Payout notifications are disabled, cannot send test email");
            return;
        }

        log.info("🧪 Sending test payout notification");
        
        // First, let's debug the configuration
        debugEmailConfiguration();

        try {
            String subject = "🧪 Test Payout Notification - " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String content = buildTestNotificationContent();

            sendNotificationToAdmins(subject, content);
            
            log.info("🧪 Test notification sending completed");

        } catch (Exception e) {
            log.error("❌ Error sending test payout notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Debug email configuration to identify issues
     */
    private void debugEmailConfiguration() {
        try {
            log.info("🔍 Debugging email configuration:");
            log.info("  - Notification enabled: {}", payoutProperties.getNotification().isEnabled());
            log.info("  - Admin emails count: {}", payoutProperties.getNotification().getAdmin().getEmails().size());
            log.info("  - Admin emails: {}", payoutProperties.getNotification().getAdmin().getEmails());
            
            // Test email service availability
            log.info("  - Email service class: {}", emailService.getClass().getSimpleName());
            
        } catch (Exception e) {
            log.error("❌ Error debugging email configuration: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Build content for test notification
     */
    private String buildTestNotificationContent() {
        StringBuilder content = new StringBuilder();
        content.append("<h3>🧪 Test Payout Notification</h3>");
        content.append("<div style='margin: 20px 0; padding: 10px; background-color: #e3f2fd; border-left: 4px solid #2196f3;'>");
        content.append("<p><strong>Test Time:</strong> ").append(LocalDateTime.now().format(FORMATTER)).append("</p>");
        content.append("<p><strong>Purpose:</strong> Verify email notification system is working</p>");
        content.append("<p><strong>System Status:</strong> ✅ Email service is operational</p>");
        content.append("</div>");

        content.append("<h4>📧 Configuration Details</h4>");
        content.append("<ul>");
        content.append("<li><strong>Notification Enabled:</strong> ").append(payoutProperties.getNotification().isEnabled()).append("</li>");
        content.append("<li><strong>Admin Emails:</strong> ").append(String.join(", ", payoutProperties.getNotification().getAdmin().getEmails())).append("</li>");
        content.append("<li><strong>Scheduling Enabled:</strong> ").append(payoutProperties.getScheduling().isEnabled()).append("</li>");
        content.append("</ul>");

        content.append("<div style='margin: 20px 0; padding: 10px; background-color: #e8f5e8; border-left: 4px solid #4caf50;'>");
        content.append("<p>✅ <strong>Success!</strong> If you receive this email, the notification system is working correctly.</p>");
        content.append("</div>");

        content.append("<hr>");
        content.append("<p><em>This is a test notification from the KTC Learning Platform payout system.</em></p>");

        return content.toString();
    }
}
