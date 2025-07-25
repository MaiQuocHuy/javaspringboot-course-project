package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.lesson.entity.Lesson;

import java.time.LocalDateTime;

@Entity
@Table(name = "QUIZ_QUESTION", indexes = {
        @Index(name = "idx_lesson", columnList = "lesson_id")
})
@Getter
@Setter
public class QuizQuestion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "question_text", columnDefinition = "text", nullable = false)
    private String questionText;

    @Column(columnDefinition = "json", nullable = false)
    private String options;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}