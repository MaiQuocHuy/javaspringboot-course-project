package project.ktc.springboot_app.instructor_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_applications", uniqueConstraints = @UniqueConstraint(name = "unique_app_user", columnNames = {
                "user_id" }), indexes = {
                                @Index(name = "idx_app_status", columnList = "status")
                })
@Getter
@Setter
public class InstructorApplication {

        @Id
        @Column(length = 36, updatable = false, nullable = false)
        private String id = UUID.randomUUID().toString();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "reviewed_by")
        private User reviewedBy;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ApplicationStatus status = ApplicationStatus.PENDING;

        @Column(columnDefinition = "JSON")
        private String documents;

        @CreationTimestamp
        @Column(name = "submitted_at", updatable = false)
        private LocalDateTime submittedAt;

        @Column(name = "reviewed_at")
        private LocalDateTime reviewedAt;

        @Column(name = "rejection_reason", columnDefinition = "TEXT")
        private String rejectionReason;

        public enum ApplicationStatus {
                PENDING, APPROVED, REJECTED
        }
}