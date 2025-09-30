package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lesson_types")
@Getter
@Setter
public class LessonType extends BaseEntity {
  @Column(name = "name", length = 50, nullable = false, unique = true)
  private String name;
}
