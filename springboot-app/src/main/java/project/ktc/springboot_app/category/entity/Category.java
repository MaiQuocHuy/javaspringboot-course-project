package project.ktc.springboot_app.category.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends BaseEntity {
  @Column(nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @ManyToMany(mappedBy = "categories")
  private List<Course> courses;
}
