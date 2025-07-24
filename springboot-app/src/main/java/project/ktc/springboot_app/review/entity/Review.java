package project.ktc.springboot_app.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "REVIEW", uniqueConstraints = @UniqueConstraint(name = "unique_review", columnNames = { "user_id",
        "course_id" }))
@Getter
@Setter
public class Review {
    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.reviewedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}