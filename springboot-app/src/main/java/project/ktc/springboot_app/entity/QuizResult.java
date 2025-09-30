package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.lesson.entity.Lesson;

@Entity
@Table(
    name = "quiz_results",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_quiz_res",
            columnNames = {"user_id", "lesson_id"}))
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

  @Column(columnDefinition = "JSON")
  private String answers;

  @CreationTimestamp
  @Column(name = "completed_at", updatable = false)
  private LocalDateTime completedAt;
}
