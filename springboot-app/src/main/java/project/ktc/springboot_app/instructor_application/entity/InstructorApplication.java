package project.ktc.springboot_app.instructor_application.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;

@Entity
@Table(name = "instructor_applications", indexes = {
		@Index(name = "idx_app_status", columnList = "status"),
		@Index(name = "idx_user_submitted", columnList = "user_id, submitted_at")
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

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	public enum ApplicationStatus {
		PENDING, APPROVED, REJECTED
	}
}
