package project.ktc.springboot_app.lesson.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.lesson.entity.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {
}