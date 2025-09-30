package project.ktc.springboot_app.email.providers;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.dto.EmailAttachment;
import project.ktc.springboot_app.email.dto.EmailInlineImage;
import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;
import project.ktc.springboot_app.email.interfaces.EmailProvider;
import project.ktc.springboot_app.email.interfaces.EmailTemplateService;

/** SendGrid email provider implementation */
@Component
@ConditionalOnProperty(prefix = "app.email.provider", name = "primary", havingValue = "sendgrid")
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailProvider implements EmailProvider {

  private final SendGrid sendGridClient;
  private final EmailTemplateService templateService;
  private final EmailConfig emailConfig;

  @Override
  public EmailSendResult sendEmail(EmailRequest emailRequest) {
    log.info("Sending email via SendGrid to: {}", emailRequest.getTo());

    try {
      Mail mail = buildMail(emailRequest);

      Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());

      Response response = sendGridClient.api(request);

      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
        String messageId = extractMessageId(response);
        log.info(
            "Email sent successfully via SendGrid. StatusCode: {}, MessageId: {}",
            response.getStatusCode(),
            messageId);
        return EmailSendResult.success(messageId, getProviderName());
      } else {
        String error =
            String.format(
                "SendGrid API error. StatusCode: %d, Body: %s",
                response.getStatusCode(), response.getBody());
        log.error(error);
        return EmailSendResult.failure(error, getProviderName());
      }

    } catch (Exception e) {
      log.error("Failed to send email via SendGrid", e);
      return EmailSendResult.failure("SendGrid send failed: " + e.getMessage(), getProviderName());
    }
  }

  @Override
  public boolean isAvailable() {
    try {
      EmailConfig.Sendgrid sendgrid = emailConfig.getSendgrid();
      return StringUtils.hasText(sendgrid.getApiKey()) && StringUtils.hasText(sendgrid.getFrom());
    } catch (Exception e) {
      log.warn("SendGrid provider availability check failed", e);
      return false;
    }
  }

  @Override
  public String getProviderName() {
    return "sendgrid";
  }

  @Override
  public int getPriority() {
    return 2; // Secondary provider
  }

  private Mail buildMail(EmailRequest emailRequest) throws Exception {
    EmailConfig.Sendgrid sendgrid = emailConfig.getSendgrid();

    // From address
    Email from;
    if (StringUtils.hasText(emailRequest.getFrom())) {
      from = new Email(emailRequest.getFrom());
    } else {
      from = new Email(sendgrid.getFrom(), sendgrid.getFromName());
    }

    // Subject
    String subject = emailRequest.getSubject();

    // Primary recipient (SendGrid requires at least one)
    Email to = new Email(emailRequest.getTo().get(0));

    Mail mail = new Mail(from, subject, to, new Content("text/plain", ""));

    // Add all recipients
    setupRecipients(mail, emailRequest);

    // Setup content
    setupContent(mail, emailRequest);

    // Add attachments
    addAttachments(mail, emailRequest);

    // Setup tracking
    setupTracking(mail, sendgrid);

    // Reply-to
    if (StringUtils.hasText(emailRequest.getReplyTo())) {
      Email replyToEmail = new Email(emailRequest.getReplyTo());
      mail.setReplyTo(replyToEmail);
    }

    return mail;
  }

  private void setupRecipients(Mail mail, EmailRequest emailRequest) {
    Personalization personalization = new Personalization();

    // To addresses
    for (String toEmail : emailRequest.getTo()) {
      personalization.addTo(new Email(toEmail));
    }

    // CC addresses
    if (emailRequest.getCc() != null) {
      for (String ccEmail : emailRequest.getCc()) {
        personalization.addCc(new Email(ccEmail));
      }
    }

    // BCC addresses
    if (emailRequest.getBcc() != null) {
      for (String bccEmail : emailRequest.getBcc()) {
        personalization.addBcc(new Email(bccEmail));
      }
    }

    mail.addPersonalization(personalization);
  }

  private void setupContent(Mail mail, EmailRequest emailRequest) throws Exception {
    String htmlContent = null;
    String textContent = null;

    // Process template if provided
    if (StringUtils.hasText(emailRequest.getTemplateName())) {
      try {
        htmlContent =
            templateService.processTemplate(
                emailRequest.getTemplateName(), emailRequest.getTemplateVariables());
      } catch (Exception e) {
        log.error("Failed to process template: {}", emailRequest.getTemplateName(), e);
        throw new Exception("Template processing failed: " + e.getMessage());
      }
    } else {
      // Use provided content
      htmlContent = emailRequest.getHtmlBody();
      textContent = emailRequest.getPlainTextBody();
    }

    // Remove the default content and add the actual content
    mail.getContent().clear();

    if (StringUtils.hasText(textContent)) {
      mail.addContent(new Content("text/plain", textContent));
    }

    if (StringUtils.hasText(htmlContent)) {
      mail.addContent(new Content("text/html", htmlContent));
    }

    // If no content was provided, throw exception
    if (mail.getContent().isEmpty()) {
      throw new Exception("No email content provided");
    }
  }

  private void addAttachments(Mail mail, EmailRequest emailRequest) {
    if (emailRequest.getAttachments() != null) {
      for (EmailAttachment attachment : emailRequest.getAttachments()) {
        try {
          Attachments sendGridAttachment = new Attachments();
          sendGridAttachment.setFilename(attachment.getFilename());
          sendGridAttachment.setType(attachment.getContentType());
          sendGridAttachment.setContent(
              Base64.getEncoder().encodeToString(attachment.getContent()));
          sendGridAttachment.setDisposition("attachment");

          mail.addAttachments(sendGridAttachment);
          log.debug("Added attachment: {}", attachment.getFilename());
        } catch (Exception e) {
          log.error("Failed to add attachment: {}", attachment.getFilename(), e);
        }
      }
    }

    // Handle inline images as attachments with content-id
    if (emailRequest.getInlineImages() != null) {
      for (EmailInlineImage image : emailRequest.getInlineImages()) {
        try {
          Attachments inlineAttachment = new Attachments();
          inlineAttachment.setFilename(image.getContentId());
          inlineAttachment.setType(image.getContentType());
          inlineAttachment.setContent(Base64.getEncoder().encodeToString(image.getContent()));
          inlineAttachment.setDisposition("inline");
          inlineAttachment.setContentId(image.getContentId());

          mail.addAttachments(inlineAttachment);
          log.debug("Added inline image: {}", image.getContentId());
        } catch (Exception e) {
          log.error("Failed to add inline image: {}", image.getContentId(), e);
        }
      }
    }
  }

  private void setupTracking(Mail mail, EmailConfig.Sendgrid sendgrid) {
    // Note: Tracking settings are available in SendGrid but require specific import
    // For now, we'll rely on SendGrid's default tracking settings
    // This can be enhanced later with proper tracking configuration
    log.debug("Using default SendGrid tracking settings");
  }

  private String extractMessageId(Response response) {
    try {
      // SendGrid returns message ID in headers
      if (response.getHeaders() != null && response.getHeaders().containsKey("x-message-id")) {
        return response.getHeaders().get("x-message-id");
      }

      // Fallback to generating a unique ID
      return "sendgrid-" + System.currentTimeMillis();
    } catch (Exception e) {
      log.debug("Could not extract message ID from SendGrid response", e);
      return "sendgrid-" + System.currentTimeMillis();
    }
  }
}
