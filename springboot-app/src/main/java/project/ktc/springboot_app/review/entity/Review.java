package project.ktc.springboot_app.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_review",
            columnNames = {"user_id", "course_id"}))
@Check(constraints = "rating >= 1 AND rating <= 5")
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
  private Integer rating = 5;

  @Column(name = "review_text", columnDefinition = "TEXT")
  private String reviewText;

  @CreationTimestamp
  @Column(name = "reviewed_at", updatable = false)
  private LocalDateTime reviewedAt;
}
