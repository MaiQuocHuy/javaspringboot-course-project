package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.lesson.entity.Lesson;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_completions", uniqueConstraints = @UniqueConstraint(name = "unique_completion", columnNames = {
        "user_id", "lesson_id" }))
@Getter
@Setter
public class LessonCompletion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @CreationTimestamp
    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;
}