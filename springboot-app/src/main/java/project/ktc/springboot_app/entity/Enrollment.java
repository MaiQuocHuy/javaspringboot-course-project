package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "ENROLLMENT", uniqueConstraints = @UniqueConstraint(name = "unique_enrollment", columnNames = {"user_id", "course_id"}))
@Getter
@Setter
public class Enrollment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "completion_status")
    private String completionStatus = "IN_PROGRESS";
} 