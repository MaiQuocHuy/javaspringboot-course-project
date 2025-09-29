package project.ktc.springboot_app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Processor to handle payout email configuration parsing
 * Ensures that comma-separated email strings are properly parsed into lists
 */
@Component
@Slf4j
public class PayoutEmailConfigProcessor {

    @Value("${app.payout.notification.admin.emails:admin@ktc-learning.com,maiquochuy16122003@gmail.com}")
    private String rawAdminEmails;

    private final PayoutSchedulingProperties payoutProperties;

    public PayoutEmailConfigProcessor(PayoutSchedulingProperties payoutProperties) {
        this.payoutProperties = payoutProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void processEmailConfiguration() {
        log.info("🔧 Processing payout email configuration...");
        
        try {
            // Parse the raw email string
            List<String> emailList = parseEmailString(rawAdminEmails);
            
            log.info("📧 Raw admin emails string: '{}'", rawAdminEmails);
            log.info("📧 Parsed admin emails: {}", emailList);
            log.info("📧 Email count: {}", emailList.size());
            
            // Update the configuration if needed
            if (emailList.size() > 1 && payoutProperties.getNotification().getAdmin().getEmails().size() == 1) {
                log.info("🔄 Updating admin emails list from {} to {}", 
                        payoutProperties.getNotification().getAdmin().getEmails(), emailList);
                
                // Force update the email list
                payoutProperties.getNotification().getAdmin().setEmails(emailList);
                
                log.info("✅ Admin emails updated successfully: {}", 
                        payoutProperties.getNotification().getAdmin().getEmails());
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing email configuration: {}", e.getMessage(), e);
        }
    }
    
    private List<String> parseEmailString(String emailString) {
        if (emailString == null || emailString.trim().isEmpty()) {
            return List.of("admin@ktc-learning.com");
        }
        
        // Split by comma and clean up
        return Arrays.stream(emailString.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }
    
    /**
     * Get the current admin emails for debugging
     */
    public List<String> getCurrentAdminEmails() {
        return payoutProperties.getNotification().getAdmin().getEmails();
    }
    
    /**
     * Get the raw email configuration string
     */
    public String getRawEmailConfiguration() {
        return rawAdminEmails;
    }
}
