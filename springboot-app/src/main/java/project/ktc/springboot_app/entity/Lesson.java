package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.section.entity.Section;

import java.util.List;

@Entity
@Table(name = "LESSON", uniqueConstraints = @UniqueConstraint(name = "unique_lesson_order", columnNames = {"section_id", "order_index"}))
@Getter
@Setter
public class Lesson extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String type;

    @Column(name = "content_id")
    private String contentId;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonCompletion> completions;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> quizQuestions;
} 