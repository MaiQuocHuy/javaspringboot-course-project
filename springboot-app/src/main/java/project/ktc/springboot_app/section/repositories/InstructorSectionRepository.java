package project.ktc.springboot_app.section.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.section.entity.Section;

import java.util.List;

@Repository
public interface InstructorSectionRepository extends JpaRepository<Section, String> {

    @Query("SELECT s FROM Section s " +
            "WHERE s.course.id = :courseId " +
            "ORDER BY s.orderIndex ASC")
    List<Section> findSectionsByCourseIdOrderByOrder(@Param("courseId") String courseId);

    @Query("SELECT s FROM Section s " +
            "WHERE s.course.id = :courseId " +
            "AND s.orderIndex > :orderIndex " +
            "ORDER BY s.orderIndex ASC")
    List<Section> findSectionsByCourseIdAndOrderGreaterThan(@Param("courseId") String courseId,
            @Param("orderIndex") Integer orderIndex);

    @Query("SELECT COUNT(s) FROM Section s WHERE s.course.id = :courseId")
    Long countSectionsByCourseId(@Param("courseId") String courseId);
}
