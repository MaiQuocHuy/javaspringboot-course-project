package project.ktc.springboot_app.certificate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;

@Entity
@Table(name = "certificates", uniqueConstraints = {
		@UniqueConstraint(name = "unique_user_course_certificate", columnNames = { "user_id", "course_id" }),
		@UniqueConstraint(name = "unique_certificate_code", columnNames = { "certificate_code" })
})
@Getter
@Setter
public class Certificate {
	@Id
	@Column(name = "certificate_id", nullable = false, unique = true, length = 36)
	private String id = UUID.randomUUID().toString();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(name = "issued_at", nullable = false)
	private LocalDateTime issuedAt;

	@Column(name = "certificate_code", nullable = false, unique = true, length = 100)
	private String certificateCode;

	@Column(name = "file_url", length = 500)
	private String fileUrl;

	@PrePersist
	protected void onCreate() {
		if (issuedAt == null) {
			issuedAt = LocalDateTime.now();
		}
	}

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
