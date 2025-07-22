package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "INSTRUCTOR_APPLICATION", uniqueConstraints = @UniqueConstraint(name = "unique_application", columnNames = {"user_id"}), indexes = {
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
public class InstructorApplication extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(columnDefinition = "json")
    private String documents;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "text")
    private String rejectionReason;
} 