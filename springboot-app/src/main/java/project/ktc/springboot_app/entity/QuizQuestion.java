package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.lesson.entity.Lesson;

@Entity
@Table(
    name = "quiz_questions",
    indexes = {@Index(name = "idx_qq_lesson", columnList = "lesson_id")})
@Getter
@Setter
public class QuizQuestion extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lesson_id", nullable = false)
  private Lesson lesson;

  @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
  private String questionText;

  @Column(columnDefinition = "JSON", nullable = false)
  private String options;

  @Column(name = "correct_answer", nullable = false)
  private String correctAnswer;

  @Column(columnDefinition = "TEXT")
  private String explanation;
}
