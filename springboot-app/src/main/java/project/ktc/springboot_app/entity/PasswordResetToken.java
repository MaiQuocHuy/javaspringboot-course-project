package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import project.ktc.springboot_app.auth.entitiy.User;

/**
 * Entity representing password reset tokens for OTP-based password reset functionality.
 *
 * <p>This entity stores One-Time Passwords (OTPs) that are sent to users via email when they
 * request to reset their password. The tokens have expiration times and attempt limits to ensure
 * security.
 *
 * <p>Security features: - OTP expires after a configurable time (default: 15 minutes) - Limited
 * number of verification attempts (default: 3) - Single-use tokens (marked as used after successful
 * verification) - Automatic cleanup of expired tokens
 *
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Entity
@Table(
    name = "password_reset_tokens",
    indexes = {
      @Index(name = "idx_password_reset_user_id", columnList = "user_id"),
      @Index(name = "idx_password_reset_otp_expires", columnList = "otp_code, expires_at"),
      @Index(name = "idx_password_reset_created_at", columnList = "created_at")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken extends BaseEntity {

  /** The user who requested the password reset. Foreign key reference to the users table. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_password_reset_tokens_user"))
  private User user;

  /**
   * The One-Time Password (OTP) code sent to the user. Typically a 6-digit numeric code for
   * security and usability.
   */
  @Column(name = "otp_code", nullable = false, length = 6)
  private String otpCode;

  /**
   * The timestamp when this token expires. After this time, the OTP cannot be used for password
   * reset.
   */
  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  /**
   * Flag indicating whether this token has been used. Once a token is used successfully, it cannot
   * be reused.
   */
  @Column(name = "is_used", nullable = false)
  @Builder.Default
  private Boolean isUsed = false;

  /**
   * Number of failed verification attempts for this token. Used to prevent brute force attacks on
   * OTP codes.
   */
  @Column(name = "attempts", nullable = false)
  @Builder.Default
  private Integer attempts = 0;

  /**
   * Maximum number of verification attempts allowed. After this limit is reached, the token becomes
   * invalid.
   */
  @Column(name = "max_attempts", nullable = false)
  @Builder.Default
  private Integer maxAttempts = 3;

  // Utility methods for business logic

  /**
   * Checks if the token is expired based on the current time.
   *
   * @return true if the token has expired, false otherwise
   */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiresAt);
  }

  /**
   * Checks if the token has reached the maximum number of attempts.
   *
   * @return true if max attempts reached, false otherwise
   */
  public boolean isMaxAttemptsReached() {
    return this.attempts >= this.maxAttempts;
  }

  /**
   * Checks if the token is valid for use. A token is valid if it's not used, not expired, and
   * hasn't reached max attempts.
   *
   * @return true if the token is valid, false otherwise
   */
  public boolean isValid() {
    return !this.isUsed && !this.isExpired() && !this.isMaxAttemptsReached();
  }

  /** Increments the attempt counter by 1. Used when an incorrect OTP is provided. */
  public void incrementAttempts() {
    this.attempts++;
  }

  /** Marks this token as used. Called after successful OTP verification and password reset. */
  public void markAsUsed() {
    this.isUsed = true;
  }

  /**
   * Returns the remaining attempts before the token becomes invalid.
   *
   * @return number of attempts remaining
   */
  public int getRemainingAttempts() {
    return Math.max(0, this.maxAttempts - this.attempts);
  }
}
