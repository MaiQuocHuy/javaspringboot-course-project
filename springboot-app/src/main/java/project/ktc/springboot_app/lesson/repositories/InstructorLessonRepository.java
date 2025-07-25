package project.ktc.springboot_app.lesson.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.lesson.entity.Lesson;

import java.util.List;

@Repository
public interface InstructorLessonRepository extends JpaRepository<Lesson, String> {

        @Query("SELECT l FROM Lesson l " +
                        "WHERE l.section.id = :sectionId " +
                        "ORDER BY l.orderIndex ASC")
        List<Lesson> findLessonsBySectionIdOrderByOrder(@Param("sectionId") String sectionId);

        @Query("SELECT l FROM Lesson l " +
                        "WHERE l.section.id = :sectionId " +
                        "AND l.orderIndex > :orderIndex " +
                        "ORDER BY l.orderIndex ASC")
        List<Lesson> findLessonsBySectionIdAndOrderGreaterThan(@Param("sectionId") String sectionId,
                        @Param("orderIndex") int orderIndex);

        @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.id = :sectionId")
        Long countLessonsBySectionId(@Param("sectionId") String sectionId);
}
