package project.ktc.springboot_app.email.interfaces;

import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Email service interface for sending emails
 * Supports both synchronous and asynchronous email sending
 */
public interface EmailService {

        /**
         * Send email synchronously
         * 
         * @param emailRequest the email request
         * @return email send result
         */
        EmailSendResult sendEmail(EmailRequest emailRequest);

        /**
         * Send email asynchronously
         * 
         * @param emailRequest the email request
         * @return future with email send result
         */
        CompletableFuture<EmailSendResult> sendEmailAsync(EmailRequest emailRequest);

        /**
         * Send email with template
         * 
         * @param to                recipient email
         * @param subject           email subject
         * @param templateName      template name (without .html extension)
         * @param templateVariables template variables
         * @return email send result
         */
        EmailSendResult sendTemplateEmail(String to, String subject, String templateName,
                        java.util.Map<String, Object> templateVariables);

        /**
         * Send email with template asynchronously
         * 
         * @param to                recipient email
         * @param subject           email subject
         * @param templateName      template name (without .html extension)
         * @param templateVariables template variables
         * @return future with email send result
         */
        CompletableFuture<EmailSendResult> sendTemplateEmailAsync(String to, String subject,
                        String templateName,
                        java.util.Map<String, Object> templateVariables);

        /**
         * Send simple text email
         * 
         * @param to      recipient email
         * @param subject email subject
         * @param content email content
         * @return email send result
         */
        EmailSendResult sendSimpleEmail(String to, String subject, String content);

        /**
         * Send simple text email asynchronously
         * 
         * @param to      recipient email
         * @param subject email subject
         * @param content email content
         * @return future with email send result
         */
        CompletableFuture<EmailSendResult> sendSimpleEmailAsync(String to, String subject, String content);

        /**
         * Send payment confirmation email asynchronously
         * 
         * @param customerEmail  recipient email
         * @param customerName   customer name
         * @param courseTitle    course title
         * @param courseUrl      course access URL
         * @param instructorName instructor name
         * @param courseLevel    course level
         * @param courseDuration course duration
         * @param lessonCount    number of lessons
         * @param amount         payment amount
         * @param transactionId  transaction ID
         * @param paymentMethod  payment method
         * @param paymentDate    payment date
         * @return future with email send result
         */
        CompletableFuture<EmailSendResult> sendPaymentConfirmationEmailAsync(
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
                        java.time.LocalDateTime paymentDate);

        /**
         * Send certificate notification email asynchronously
         * 
         * @param studentEmail    Email of the student
         * @param studentName     Name of the student
         * @param courseTitle     Title of the completed course
         * @param instructorName  Name of the course instructor
         * @param certificateCode Unique certificate code
         * @param certificateUrl  URL to download the certificate
         * @param issueDate       Date when certificate was issued
         * @return CompletableFuture with email send result
         */
        CompletableFuture<EmailSendResult> sendCertificateNotificationEmailAsync(
                        String studentEmail,
                        String studentName,
                        String courseTitle,
                        String instructorName,
                        String certificateCode,
                        String certificateUrl,
                        java.time.LocalDateTime issueDate);

        /**
         * Send discount code email to all students using discount ID
         *
         * @param discountId The discount ID to fetch discount details
         * @param subject    Email subject
         * @return CompletableFuture with number of successful sends
         */
        CompletableFuture<Long> sendDiscountCodeToAllStudents(
                        String discountId,
                        String subject);

        /**
         * Send discount code to a specific user
         * 
         * @param discountId The discount ID to fetch discount details
         * @param subject    Email subject
         * @param userId     Specific user ID to send email to
         * @return CompletableFuture with number of successful sends (1 if successful, 0
         *         if failed)
         */
        CompletableFuture<Long> sendDiscountCodeToSpecificUser(
                        String discountId,
                        String subject,
                        String userId);

}
