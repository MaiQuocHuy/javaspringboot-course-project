package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "LESSON_COMPLETION", uniqueConstraints = @UniqueConstraint(name = "unique_completion", columnNames = {"user_id", "lesson_id"}))
@Getter
@Setter
public class LessonCompletion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;
} 