package project.ktc.springboot_app.email.providers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.dto.EmailAttachment;
import project.ktc.springboot_app.email.dto.EmailInlineImage;
import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;
import project.ktc.springboot_app.email.interfaces.EmailProvider;
import project.ktc.springboot_app.email.interfaces.EmailTemplateService;

import java.util.UUID;

/**
 * SMTP email provider implementation
 */
@Component
@ConditionalOnProperty(prefix = "app.email.provider", name = "primary", havingValue = "smtp", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;
    private final EmailConfig emailConfig;

    @Override
    public EmailSendResult sendEmail(EmailRequest emailRequest) {
        log.info("Sending email via SMTP to: {}", emailRequest.getTo());

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set basic email properties
            setupBasicProperties(helper, emailRequest);

            // Set email content
            setupEmailContent(helper, emailRequest);

            // Add attachments
            addAttachments(helper, emailRequest);

            // Add inline images
            addInlineImages(helper, emailRequest);

            // Send the email
            mailSender.send(mimeMessage);

            String messageId = UUID.randomUUID().toString();
            log.info("Email sent successfully via SMTP. MessageId: {}", messageId);

            return EmailSendResult.success(messageId, getProviderName());

        } catch (Exception e) {
            log.error("Failed to send email via SMTP", e);
            return EmailSendResult.failure("SMTP send failed: " + e.getMessage(), getProviderName());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check if SMTP configuration is valid
            EmailConfig.Smtp smtp = emailConfig.getSmtp();
            return StringUtils.hasText(smtp.getHost()) &&
                    smtp.getPort() > 0 &&
                    StringUtils.hasText(smtp.getFrom());
        } catch (Exception e) {
            log.warn("SMTP provider availability check failed", e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "smtp";
    }

    @Override
    public int getPriority() {
        return 1; // Primary provider
    }

    private void setupBasicProperties(MimeMessageHelper helper, EmailRequest emailRequest) throws MessagingException {
        EmailConfig.Smtp smtp = emailConfig.getSmtp();

        // From address
        if (StringUtils.hasText(emailRequest.getFrom())) {
            helper.setFrom(emailRequest.getFrom());
        } else {
            if (StringUtils.hasText(smtp.getFromName())) {
                try {
                    helper.setFrom(smtp.getFrom(), smtp.getFromName());
                } catch (java.io.UnsupportedEncodingException e) {
                    log.warn("Failed to set from name, using email only", e);
                    helper.setFrom(smtp.getFrom());
                }
            } else {
                helper.setFrom(smtp.getFrom());
            }
        }

        // To addresses
        helper.setTo(emailRequest.getTo().toArray(new String[0]));

        // CC addresses
        if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
            helper.setCc(emailRequest.getCc().toArray(new String[0]));
        }

        // BCC addresses
        if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
            helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
        }

        // Subject
        helper.setSubject(emailRequest.getSubject());

        // Reply-to
        if (StringUtils.hasText(emailRequest.getReplyTo())) {
            helper.setReplyTo(emailRequest.getReplyTo());
        }
    }

    private void setupEmailContent(MimeMessageHelper helper, EmailRequest emailRequest) throws MessagingException {
        String htmlContent = null;
        String textContent = null;

        // Process template if provided
        if (StringUtils.hasText(emailRequest.getTemplateName())) {
            try {
                htmlContent = templateService.processTemplate(
                        emailRequest.getTemplateName(),
                        emailRequest.getTemplateVariables());
            } catch (Exception e) {
                log.error("Failed to process template: {}", emailRequest.getTemplateName(), e);
                throw new MessagingException("Template processing failed: " + e.getMessage());
            }
        } else {
            // Use provided content
            htmlContent = emailRequest.getHtmlBody();
            textContent = emailRequest.getPlainTextBody();
        }

        // Set content based on what's available
        if (StringUtils.hasText(htmlContent) && StringUtils.hasText(textContent)) {
            helper.setText(textContent, htmlContent);
        } else if (StringUtils.hasText(htmlContent)) {
            helper.setText(htmlContent, true);
        } else if (StringUtils.hasText(textContent)) {
            helper.setText(textContent, false);
        } else {
            throw new MessagingException("No email content provided");
        }
    }

    private void addAttachments(MimeMessageHelper helper, EmailRequest emailRequest) throws MessagingException {
        if (emailRequest.getAttachments() != null) {
            for (EmailAttachment attachment : emailRequest.getAttachments()) {
                try {
                    ByteArrayResource resource = new ByteArrayResource(attachment.getContent());
                    helper.addAttachment(attachment.getFilename(), resource, attachment.getContentType());
                    log.debug("Added attachment: {}", attachment.getFilename());
                } catch (Exception e) {
                    log.error("Failed to add attachment: {}", attachment.getFilename(), e);
                    throw new MessagingException("Failed to add attachment: " + attachment.getFilename());
                }
            }
        }
    }

    private void addInlineImages(MimeMessageHelper helper, EmailRequest emailRequest) throws MessagingException {
        if (emailRequest.getInlineImages() != null) {
            for (EmailInlineImage image : emailRequest.getInlineImages()) {
                try {
                    ByteArrayResource resource = new ByteArrayResource(image.getContent());
                    helper.addInline(image.getContentId(), resource, image.getContentType());
                    log.debug("Added inline image: {}", image.getContentId());
                } catch (Exception e) {
                    log.error("Failed to add inline image: {}", image.getContentId(), e);
                    throw new MessagingException("Failed to add inline image: " + image.getContentId());
                }
            }
        }
    }
}
