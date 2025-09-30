package project.ktc.springboot_app.refresh_token.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
	Optional<RefreshToken> findByToken(String token);

	/** Find all active (non-revoked and non-expired) tokens for a user */
	@Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > :currentTime")
	List<RefreshToken> findActiveTokensByUserId(
			@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);

	/** Count active tokens for a user */
	@Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false AND rt.expiresAt > :currentTime")
	long countActiveTokensByUserId(
			@Param("userId") String userId, @Param("currentTime") LocalDateTime currentTime);
}
