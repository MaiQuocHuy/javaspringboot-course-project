package project.ktc.springboot_app.lesson.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.entity.LessonType;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.section.entity.Section;

@Entity
@Table(name = "lessons", uniqueConstraints = @UniqueConstraint(name = "unique_lesson_order", columnNames = {
		"section_id", "order_index" }))
@Getter
@Setter
public class Lesson extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", nullable = false)
	private Section section;

	@Column(nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lesson_type_id", nullable = false)
	private LessonType lessonType;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id")
	private VideoContent content;

	@Column(name = "order_index")
	private Integer orderIndex = 0;

	@OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LessonCompletion> completions;

	@OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<QuizQuestion> quizQuestions;
}
