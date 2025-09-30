package project.ktc.springboot_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.PasswordResetToken;

/**
 * Repository interface for PasswordResetToken entities.
 *
 * <p>
 * Provides data access methods for managing password reset tokens including OTP
 * verification,
 * token cleanup, and user-specific token management.
 *
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

	/**
	 * Finds a valid password reset token by user ID and OTP code. A valid token is
	 * one that is not
	 * used, not expired, and hasn't reached max attempts.
	 *
	 * @param userId
	 *            the user ID
	 * @param otpCode
	 *            the OTP code
	 * @param currentTime
	 *            the current timestamp for expiration check
	 * @return Optional containing the token if found and valid
	 */
	@Query("SELECT prt FROM PasswordResetToken prt "
			+ "WHERE prt.user.id = :userId "
			+ "AND prt.otpCode = :otpCode "
			+ "AND prt.isUsed = false "
			+ "AND prt.expiresAt > :currentTime "
			+ "AND prt.attempts < prt.maxAttempts "
			+ "ORDER BY prt.createdAt DESC")
	Optional<PasswordResetToken> findValidTokenByUserIdAndOtpCode(
			@Param("userId") String userId,
			@Param("otpCode") String otpCode,
			@Param("currentTime") LocalDateTime currentTime);

	/**
	 * Finds the most recent password reset token for a user. Useful for checking
	 * rate limiting and
	 * preventing spam requests.
	 *
	 * @param userId
	 *            the user ID
	 * @return Optional containing the most recent token if exists
	 */
	@Query(value = "SELECT * FROM password_reset_tokens prt "
			+ "WHERE prt.user_id = :userId "
			+ "ORDER BY prt.created_at DESC LIMIT 1", nativeQuery = true)
	Optional<PasswordResetToken> findLatestTokenByUserId(@Param("userId") String userId);

	/**
	 * Finds all unexpired and unused tokens for a specific user. Used for
	 * invalidating previous
	 * tokens when a new one is generated.
	 *
	 * @param userId
	 *            the user ID
	 * @param currentTime
	 *            the current timestamp for expiration check
	 * @return List of active tokens
	 */
	@Query("SELECT prt FROM PasswordResetToken prt "
			+ "WHERE prt.user.id = :userId "
			+ "AND prt.isUsed = false "
			+ "AND prt.expiresAt > :currentTime")
	List<PasswordResetToken> findActiveTokensByUserId(
			@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);

	/**
	 * Invalidates (marks as used) all active tokens for a user. Called when a new
	 * password reset
	 * token is generated to ensure only one valid token exists.
	 *
	 * @param userId
	 *            the user ID
	 * @param currentTime
	 *            the current timestamp for expiration check
	 * @return number of tokens invalidated
	 */
	@Modifying
	@Query("UPDATE PasswordResetToken prt "
			+ "SET prt.isUsed = true, prt.updatedAt = :currentTime "
			+ "WHERE prt.user.id = :userId "
			+ "AND prt.isUsed = false "
			+ "AND prt.expiresAt > :currentTime")
	int invalidateActiveTokensByUserId(
			@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);

	/**
	 * Cleans up expired tokens from the database. Should be called periodically
	 * (e.g., via scheduled
	 * task) to maintain database performance.
	 *
	 * @param currentTime
	 *            the current timestamp
	 * @return number of tokens deleted
	 */
	@Modifying
	@Query("DELETE FROM PasswordResetToken prt " + "WHERE prt.expiresAt < :currentTime")
	int deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

	/**
	 * Cleans up old used tokens to prevent database bloat. Removes tokens that were
	 * used more than a
	 * specified number of days ago.
	 *
	 * @param cutoffTime
	 *            the cutoff timestamp (tokens older than this will be deleted)
	 * @return number of tokens deleted
	 */
	@Modifying
	@Query("DELETE FROM PasswordResetToken prt "
			+ "WHERE prt.isUsed = true "
			+ "AND prt.updatedAt < :cutoffTime")
	int deleteOldUsedTokens(@Param("cutoffTime") LocalDateTime cutoffTime);

	/**
	 * Counts the number of password reset requests made by a user within a time
	 * window. Used for
	 * implementing rate limiting to prevent abuse.
	 *
	 * @param userId
	 *            the user ID
	 * @param fromTime
	 *            the start of the time window
	 * @return count of requests in the time window
	 */
	@Query("SELECT COUNT(prt) FROM PasswordResetToken prt "
			+ "WHERE prt.user.id = :userId "
			+ "AND prt.createdAt >= :fromTime")
	long countTokensCreatedByUserSince(
			@Param("userId") String userId, @Param("fromTime") LocalDateTime fromTime);

	/**
	 * Finds a token by its OTP code regardless of user (for uniqueness checks).
	 * Used to ensure OTP
	 * codes are unique across the system.
	 *
	 * @param otpCode
	 *            the OTP code
	 * @param currentTime
	 *            the current timestamp for expiration check
	 * @return Optional containing the token if found and not expired
	 */
	@Query("SELECT prt FROM PasswordResetToken prt "
			+ "WHERE prt.otpCode = :otpCode "
			+ "AND prt.expiresAt > :currentTime "
			+ "AND prt.isUsed = false")
	Optional<PasswordResetToken> findActiveTokenByOtpCode(
			@Param("otpCode") String otpCode, @Param("currentTime") LocalDateTime currentTime);

	/**
	 * Finds all tokens that will expire within a specified time. Useful for sending
	 * reminder
	 * notifications or cleanup warnings.
	 *
	 * @param expiryTime
	 *            the expiry threshold
	 * @return List of tokens expiring soon
	 */
	@Query("SELECT prt FROM PasswordResetToken prt "
			+ "WHERE prt.expiresAt <= :expiryTime "
			+ "AND prt.isUsed = false "
			+ "ORDER BY prt.expiresAt ASC")
	List<PasswordResetToken> findTokensExpiringBefore(@Param("expiryTime") LocalDateTime expiryTime);
}
