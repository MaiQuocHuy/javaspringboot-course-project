package project.ktc.springboot_app.email.interfaces;

import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;

/**
 * Email provider interface for different email service providers
 * Abstracts the underlying email sending implementation
 */
public interface EmailProvider {

    /**
     * Send email using the provider
     * 
     * @param emailRequest the email request
     * @return email send result
     */
    EmailSendResult sendEmail(EmailRequest emailRequest);

    /**
     * Check if the provider is available and configured
     * 
     * @return true if provider is available
     */
    boolean isAvailable();

    /**
     * Get the provider name
     * 
     * @return provider name (e.g., "smtp", "sendgrid")
     */
    String getProviderName();

    /**
     * Get provider priority (lower number = higher priority)
     * 
     * @return provider priority
     */
    int getPriority();

    /**
     * Check if the provider supports the given email request
     * 
     * @param emailRequest the email request
     * @return true if provider supports this request
     */
    default boolean supports(EmailRequest emailRequest) {
        return true;
    }
}
