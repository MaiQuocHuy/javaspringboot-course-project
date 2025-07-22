package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW", uniqueConstraints = @UniqueConstraint(name = "unique_review", columnNames = {"user_id", "course_id"}))
@Getter
@Setter
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "reviewed_at", updatable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 