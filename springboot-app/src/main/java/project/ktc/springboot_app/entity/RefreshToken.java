package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "REFRESH_TOKEN", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_token", columnList = "token", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @Column(length = 36)
    @Builder.Default
    private String id = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 512, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked")
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;
} 