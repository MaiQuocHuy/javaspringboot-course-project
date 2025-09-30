package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;

@Entity
@Table(name = "refresh_tokens", indexes = { @Index(name = "idx_rt_user", columnList = "user_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

	@Id
	@Column(length = 36, updatable = false, nullable = false)
	@Builder.Default
	private String id = UUID.randomUUID().toString();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 512, unique = true)
	private String token;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "is_revoked", nullable = false)
	@Builder.Default
	private Boolean isRevoked = false;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
