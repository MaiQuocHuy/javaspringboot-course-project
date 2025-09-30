package project.ktc.springboot_app.auth.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for password reset response.
 *
 * <p>This DTO is returned after successfully initiating a password reset request. It provides
 * information about the OTP that was sent without revealing the actual code.
 *
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponseDto {

  /** A message confirming that the OTP has been sent. Generic message for security reasons. */
  private String message;

  /**
   * The email address to which the OTP was sent (masked for privacy). Example:
   * "john***@example.com"
   */
  private String maskedEmail;

  /**
   * The timestamp when the OTP will expire. Helps users understand the urgency of using the code.
   */
  private LocalDateTime expiresAt;

  /** Maximum number of attempts allowed for this OTP. Informs users about the attempt limit. */
  private Integer maxAttempts;

  /**
   * Indicates whether this is a new OTP or if previous tokens were invalidated. Useful for
   * informing users about security measures.
   */
  private Boolean previousTokensInvalidated;

  /**
   * Creates a standard success response for forgot password requests.
   *
   * @param email the original email address
   * @param expiresAt when the OTP expires
   * @param maxAttempts maximum verification attempts
   * @param previousTokensInvalidated whether old tokens were invalidated
   * @return formatted response DTO
   */
  public static ForgotPasswordResponseDto success(
      String email,
      LocalDateTime expiresAt,
      Integer maxAttempts,
      Boolean previousTokensInvalidated) {

    return ForgotPasswordResponseDto.builder()
        .message(
            "OTP has been sent to your email address. Please check your inbox and spam folder.")
        .maskedEmail(maskEmail(email))
        .expiresAt(expiresAt)
        .maxAttempts(maxAttempts)
        .previousTokensInvalidated(previousTokensInvalidated)
        .build();
  }

  /**
   * Masks an email address for privacy protection. Shows first few characters and domain while
   * hiding middle part.
   *
   * @param email the email to mask
   * @return masked email string
   */
  private static String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return "***@***.***";
    }

    String[] parts = email.split("@");
    String localPart = parts[0];
    String domain = parts[1];

    // Mask local part
    String maskedLocal;
    if (localPart.length() <= 2) {
      maskedLocal = "***";
    } else if (localPart.length() <= 4) {
      maskedLocal = localPart.charAt(0) + "***";
    } else {
      maskedLocal = localPart.substring(0, 2) + "***";
    }

    // Mask domain part
    String maskedDomain;
    if (domain.contains(".")) {
      String[] domainParts = domain.split("\\.");
      maskedDomain = domainParts[0].charAt(0) + "***." + domainParts[domainParts.length - 1];
    } else {
      maskedDomain = domain.charAt(0) + "***";
    }

    return maskedLocal + "@" + maskedDomain;
  }
}
