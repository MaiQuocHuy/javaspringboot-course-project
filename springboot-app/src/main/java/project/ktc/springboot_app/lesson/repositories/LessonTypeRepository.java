package project.ktc.springboot_app.lesson.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.LessonType;

import java.util.Optional;

@Repository
public interface LessonTypeRepository extends JpaRepository<LessonType, String> {

    /**
     * Find lesson type by name
     */
    Optional<LessonType> findByName(String name);

    /**
     * Check if lesson type exists by name
     */
    boolean existsByName(String name);
}
