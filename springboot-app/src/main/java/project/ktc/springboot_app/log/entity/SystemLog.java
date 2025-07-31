package project.ktc.springboot_app.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs", indexes = {
        @Index(name = "idx_logs_user", columnList = "user_id"),
        @Index(name = "idx_logs_action", columnList = "action"),
        @Index(name = "idx_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_logs_created", columnList = "created_at")
})
@Getter
@Setter
public class SystemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 36)
    private String entityId;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Action {
        CREATE, UPDATE, DELETE
    }
}
