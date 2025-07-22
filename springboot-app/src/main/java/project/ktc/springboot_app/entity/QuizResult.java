package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "QUIZ_RESULT", uniqueConstraints = @UniqueConstraint(name = "unique_result", columnNames = {"user_id", "lesson_id"}))
@Getter
@Setter
public class QuizResult extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "json")
    private String answers;

    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;
} 