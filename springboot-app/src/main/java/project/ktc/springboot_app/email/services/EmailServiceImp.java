package project.ktc.springboot_app.email.services;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.discount.entity.Discount;
import project.ktc.springboot_app.discount.interfaces.DiscountService;
import project.ktc.springboot_app.email.config.EmailConfig;
import project.ktc.springboot_app.email.dto.EmailRequest;
import project.ktc.springboot_app.email.dto.EmailSendResult;
import project.ktc.springboot_app.email.interfaces.EmailProvider;
import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.user.repositories.UserRepository;

/**
 * Main email service implementation Handles email sending with retry logic and provider fallback
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

  private final List<EmailProvider> emailProviders;
  private final EmailConfig emailConfig;
  private final UserRepository userRepository;
  private final DiscountService discountService;

  @Override
  public EmailSendResult sendEmail(EmailRequest emailRequest) {
    log.info("Sending email to: {}, subject: {}", emailRequest.getTo(), emailRequest.getSubject());

    // Check if should send after transaction commit
    if (emailRequest.isSendAfterCommit()) {
      log.debug("Email will be sent after transaction commit");
      // This will be handled by transaction event listener
      return EmailSendResult.success(
          "Email scheduled for sending after transaction commit", "pending");
    }

    return doSendEmail(emailRequest);
  }

  @Override
  @Async("emailTaskExecutor")
  public CompletableFuture<EmailSendResult> sendEmailAsync(EmailRequest emailRequest) {
    log.info(
        "Sending email asynchronously to: {}, subject: {}",
        emailRequest.getTo(),
        emailRequest.getSubject());

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
  public EmailSendResult sendTemplateEmail(
      String to, String subject, String templateName, Map<String, Object> templateVariables) {
    EmailRequest request =
        EmailRequest.builder()
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
  public CompletableFuture<EmailSendResult> sendTemplateEmailAsync(
      String to, String subject, String templateName, Map<String, Object> templateVariables) {
    EmailRequest request =
        EmailRequest.builder()
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
    EmailRequest request =
        EmailRequest.builder()
            .to(List.of(to))
            .subject(subject)
            .htmlBody(content)
            .async(false)
            .build();

    return sendEmail(request);
  }

  @Override
  @Async("emailTaskExecutor")
  public CompletableFuture<EmailSendResult> sendSimpleEmailAsync(
      String to, String subject, String content) {
    EmailRequest request =
        EmailRequest.builder()
            .to(List.of(to))
            .subject(subject)
            .htmlBody(content)
            .async(true)
            .build();

    return sendEmailAsync(request);
  }

  /** Actually send the email using available providers */
  private EmailSendResult doSendEmail(EmailRequest emailRequest) {
    // Get primary provider
    EmailProvider primaryProvider = getProviderByName(emailConfig.getActiveProvider());

    if (primaryProvider != null && primaryProvider.isAvailable()) {
      try {
        EmailSendResult result = primaryProvider.sendEmail(emailRequest);

        if (result.isSuccess()) {
          log.debug(
              "Email sent successfully using provider: {}", primaryProvider.getProviderName());
          return result;
        }

        log.warn(
            "Primary provider {} failed: {}",
            primaryProvider.getProviderName(),
            result.getErrorMessage());

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
    EmailSendResult failureResult =
        EmailSendResult.failure("All email providers failed", "unknown");

    // Save to failed emails for retry

    return failureResult;
  }

  /** Try fallback provider */
  private EmailSendResult tryFallbackProvider(EmailRequest emailRequest, String excludeProvider) {
    String fallbackProviderName = emailConfig.getFallbackProvider();

    if (fallbackProviderName != null && !fallbackProviderName.equals(excludeProvider)) {
      EmailProvider fallbackProvider = getProviderByName(fallbackProviderName);

      if (fallbackProvider != null && fallbackProvider.isAvailable()) {
        try {
          log.info("Trying fallback provider: {}", fallbackProvider.getProviderName());
          EmailSendResult result = fallbackProvider.sendEmail(emailRequest);

          if (result.isSuccess()) {
            log.info(
                "Email sent successfully using fallback provider: {}",
                fallbackProvider.getProviderName());
            return result;
          }

          log.warn(
              "Fallback provider {} also failed: {}",
              fallbackProvider.getProviderName(),
              result.getErrorMessage());

        } catch (Exception e) {
          log.error("Fallback provider {} threw exception", fallbackProvider.getProviderName(), e);
        }
      }
    }

    // Save to failed emails for retry
    EmailSendResult failureResult =
        EmailSendResult.failure("Both primary and fallback providers failed", "unknown");

    return failureResult;
  }

  /** Send payment confirmation email asynchronously */
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
      templateVariables.put(
          "paymentDateFormatted",
          paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
      templateVariables.put("year", String.valueOf(Year.now().getValue()));

      // Create email request
      EmailRequest request =
          EmailRequest.builder()
              .to(List.of(customerEmail))
              .subject("Payment Confirmation - Welcome to " + courseTitle)
              .templateName("payment-confirmation-template")
              .templateVariables(templateVariables)
              .async(true)
              .build();

      EmailSendResult result = sendEmail(request);
      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error(
          "Failed to send payment confirmation email to {}: {}", customerEmail, e.getMessage(), e);
      EmailSendResult errorResult =
          EmailSendResult.builder()
              .success(false)
              .errorMessage("Failed to send payment confirmation email: " + e.getMessage())
              .build();
      return CompletableFuture.completedFuture(errorResult);
    }
  }

  /** Send certificate notification email asynchronously */
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
      templateVariables.put(
          "issueDate", issueDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
      templateVariables.put("year", String.valueOf(Year.now().getValue()));

      // Create verification URL
      String verificationUrl =
          String.format("https://certificates.ktc.edu/verify/%s", certificateCode);
      templateVariables.put("verificationUrl", verificationUrl);

      // Create email request
      EmailRequest request =
          EmailRequest.builder()
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
        log.error(
            "Failed to send certificate notification email to {}: {}",
            studentEmail,
            result.getErrorMessage());
      }

      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error(
          "Failed to send certificate notification email to {}: {}",
          studentEmail,
          e.getMessage(),
          e);
      EmailSendResult errorResult =
          EmailSendResult.builder()
              .success(false)
              .errorMessage("Failed to send certificate notification email: " + e.getMessage())
              .build();
      return CompletableFuture.completedFuture(errorResult);
    }
  }

  @Override
  @Async("emailTaskExecutor")
  public CompletableFuture<Long> sendDiscountCodeToAllStudents(String discountId, String subject) {

    log.info("Starting to send discount ID {} to all students", discountId);

    try {
      // Get discount details from database
      Discount discount = discountService.getDiscountEntityById(discountId);
      if (discount == null) {
        log.error("Discount not found with ID: {}", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      // Validate discount fields
      if (discount.getCode() == null
          || discount.getStartDate() == null
          || discount.getEndDate() == null) {
        log.error("Discount with ID {} has missing required fields", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      log.info("Found discount: {} with code: {}", discount.getDescription(), discount.getCode());

      // Get all students
      List<User> students =
          userRepository
              .findUsersWithFilters(
                  null, "STUDENT", true, org.springframework.data.domain.Pageable.unpaged())
              .getContent();
      log.info("Found {} students to send discount emails", students.size());

      if (students.isEmpty()) {
        log.warn("No students found for discount email sending");
        return CompletableFuture.completedFuture(0L);
      }

      AtomicLong successCount = new AtomicLong(0);

      // Send emails to all students
      for (User student : students) {
        try {
          // Create email variables
          Map<String, Object> variables = new HashMap<>();
          variables.put("studentName", student.getName());
          variables.put("discountCode", discount.getCode());
          variables.put(
              "startDate",
              discount.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
          variables.put(
              "endDate", discount.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
          variables.put("currentYear", Year.now().getValue());

          // Create email request
          EmailRequest emailRequest =
              EmailRequest.builder()
                  .to(List.of(student.getEmail()))
                  .subject(subject)
                  .templateName("discount-code-template")
                  .templateVariables(variables)
                  .build();

          // Send email
          EmailSendResult result = doSendEmail(emailRequest);

          if (result.isSuccess()) {
            successCount.incrementAndGet();
            log.debug("Successfully sent discount email to {}", student.getEmail());
          } else {
            log.warn(
                "Failed to send discount email to {}: {}",
                student.getEmail(),
                result.getErrorMessage());
          }

          // Small delay to avoid overwhelming email provider
          Thread.sleep(100);

        } catch (Exception e) {
          log.error(
              "Error sending discount email to {}: {}", student.getEmail(), e.getMessage(), e);
        }
      }

      long totalSent = successCount.get();
      log.info("Completed sending discount emails. Success: {}/{}", totalSent, students.size());

      return CompletableFuture.completedFuture(totalSent);

    } catch (Exception e) {
      log.error("Failed to send discount emails: {}", e.getMessage(), e);
      return CompletableFuture.completedFuture(0L);
    }
  }

  @Override
  @Async("emailTaskExecutor")
  public CompletableFuture<Long> sendDiscountCodeToSpecificUser(
      String discountId, String subject, String userId) {

    log.info("Starting to send discount ID {} to user {}", discountId, userId);

    try {
      // Get discount details from database
      Discount discount = discountService.getDiscountEntityById(discountId);
      if (discount == null) {
        log.error("Discount not found with ID: {}", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      // Validate discount fields
      if (discount.getCode() == null
          || discount.getStartDate() == null
          || discount.getEndDate() == null) {
        log.error("Discount with ID {} has missing required fields", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      log.info("Found discount: {} with code: {}", discount.getDescription(), discount.getCode());

      // Get specific user
      Optional<User> userOptional = userRepository.findById(userId);
      if (userOptional.isEmpty()) {
        log.error("User not found with ID: {}", userId);
        return CompletableFuture.completedFuture(0L);
      }

      User user = userOptional.get();
      if (!user.getIsActive()) {
        log.error("User with ID {} is not active", userId);
        return CompletableFuture.completedFuture(0L);
      }

      log.info("Found user: {} ({})", user.getName(), user.getEmail());

      try {
        // Create email variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("studentName", user.getName());
        variables.put("discountCode", discount.getCode());
        variables.put(
            "startDate", discount.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put(
            "endDate", discount.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("currentYear", Year.now().getValue());

        // Create email request
        EmailRequest emailRequest =
            EmailRequest.builder()
                .to(List.of(user.getEmail()))
                .subject(subject)
                .templateName("discount-code-template")
                .templateVariables(variables)
                .build();

        // Send email
        EmailSendResult result = doSendEmail(emailRequest);

        if (result.isSuccess()) {
          log.info("Successfully sent discount email to {}", user.getEmail());
          return CompletableFuture.completedFuture(1L);
        } else {
          log.error(
              "Failed to send discount email to {}: {}", user.getEmail(), result.getErrorMessage());
          return CompletableFuture.completedFuture(0L);
        }

      } catch (Exception e) {
        log.error("Error sending discount email to {}: {}", user.getEmail(), e.getMessage(), e);
        return CompletableFuture.completedFuture(0L);
      }

    } catch (Exception e) {
      log.error("Failed to send discount email to user {}: {}", userId, e.getMessage(), e);
      return CompletableFuture.completedFuture(0L);
    }
  }

  @Override
  @Async("emailTaskExecutor")
  public CompletableFuture<Long> sendDiscountCodeToMultipleUsers(
      String discountId, String subject, List<String> userIds) {

    log.info("Starting to send discount ID {} to {} users", discountId, userIds.size());

    try {
      // Get discount details from database
      Discount discount = discountService.getDiscountEntityById(discountId);
      if (discount == null) {
        log.error("Discount not found with ID: {}", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      // Validate discount fields
      if (discount.getCode() == null
          || discount.getStartDate() == null
          || discount.getEndDate() == null) {
        log.error("Discount with ID {} has missing required fields", discountId);
        return CompletableFuture.completedFuture(0L);
      }

      log.info("Found discount: {} with code: {}", discount.getDescription(), discount.getCode());

      // Get users by IDs
      List<User> users =
          userRepository.findAllById(userIds).stream()
              .filter(user -> user.getIsActive())
              .collect(java.util.stream.Collectors.toList());

      if (users.isEmpty()) {
        log.warn("No active users found from provided user IDs");
        return CompletableFuture.completedFuture(0L);
      }

      log.info("Found {} active users out of {} requested", users.size(), userIds.size());

      // Send emails in parallel
      AtomicLong successCount = new AtomicLong(0);
      List<CompletableFuture<Void>> emailTasks =
          users.stream()
              .<CompletableFuture<Void>>map(
                  user ->
                      CompletableFuture.runAsync(
                          () -> {
                            try {
                              // Create email variables
                              Map<String, Object> variables = new HashMap<>();
                              variables.put("studentName", user.getName());
                              variables.put("discountCode", discount.getCode());
                              variables.put(
                                  "startDate",
                                  discount
                                      .getStartDate()
                                      .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                              variables.put(
                                  "endDate",
                                  discount
                                      .getEndDate()
                                      .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                              variables.put("currentYear", Year.now().getValue());

                              // Create email request
                              EmailRequest emailRequest =
                                  EmailRequest.builder()
                                      .to(List.of(user.getEmail()))
                                      .subject(subject)
                                      .templateName("discount-code-template")
                                      .templateVariables(variables)
                                      .build();

                              // Send email
                              EmailSendResult result = doSendEmail(emailRequest);

                              if (result.isSuccess()) {
                                successCount.incrementAndGet();
                                log.debug(
                                    "Successfully sent discount email to {}", user.getEmail());
                              } else {
                                log.error(
                                    "Failed to send discount email to {}: {}",
                                    user.getEmail(),
                                    result.getErrorMessage());
                              }

                            } catch (Exception e) {
                              log.error(
                                  "Error sending discount email to {}: {}",
                                  user.getEmail(),
                                  e.getMessage(),
                                  e);
                            }
                          }))
              .collect(java.util.stream.Collectors.toList());

      // Wait for all tasks to complete
      CompletableFuture<Void> allTasks =
          CompletableFuture.allOf(emailTasks.toArray(new CompletableFuture[0]));
      allTasks.join();

      long totalSent = successCount.get();
      log.info(
          "Completed sending discount emails to multiple users. Success: {}/{}",
          totalSent,
          users.size());

      return CompletableFuture.completedFuture(totalSent);

    } catch (Exception e) {
      log.error("Failed to send discount emails to multiple users: {}", e.getMessage(), e);
      return CompletableFuture.completedFuture(0L);
    }
  }

  /** Get provider by name */
  private EmailProvider getProviderByName(String providerName) {
    return emailProviders.stream()
        .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
        .findFirst()
        .orElse(null);
  }
}
