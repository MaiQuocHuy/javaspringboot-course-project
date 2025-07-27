package project.ktc.springboot_app.instructor_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "INSTRUCTOR_APPLICATION", uniqueConstraints = @UniqueConstraint(name = "unique_application", columnNames = {
        "user_id" }), indexes = {
                @Index(name = "idx_status", columnList = "status")
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