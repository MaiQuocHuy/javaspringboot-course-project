package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
	@Id
	@Column(length = 36, updatable = false, nullable = false)
	private String id = UUID.randomUUID().toString();

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}
