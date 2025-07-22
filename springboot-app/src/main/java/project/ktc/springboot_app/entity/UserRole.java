package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Entity
@Table(name = "USER_ROLE", uniqueConstraints = @UniqueConstraint(name = "unique_user_role", columnNames = {"user_id", "role"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserRole {
    @Id
    @Column(length = 36)
    @Builder.Default
    private String id = java.util.UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role;
} 