package project.ktc.springboot_app.section.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.lesson.entity.Lesson;

@Entity
@Table(
    name = "sections",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_section_order",
            columnNames = {"course_id", "order_index"}))
@Getter
@Setter
public class Section extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @Column(nullable = false)
  private String title;

  @Column(name = "order_index")
  private Integer orderIndex = 0;

  @Column(columnDefinition = "TEXT")
  private String description;

  @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Lesson> lessons;
}
