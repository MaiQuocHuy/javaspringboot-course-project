package project.ktc.springboot_app.enrollment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_enrollment",
            columnNames = {"user_id", "course_id"}))
@Getter
@Setter
public class Enrollment extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @CreationTimestamp
  @Column(name = "enrolled_at", updatable = false)
  private LocalDateTime enrolledAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "completion_status")
  private CompletionStatus completionStatus = CompletionStatus.IN_PROGRESS;

  public enum CompletionStatus {
    IN_PROGRESS,
    COMPLETED
  }
}
