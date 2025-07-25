package project.ktc.springboot_app.quiz.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.entity.QuizQuestion;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, String> {

    @Query("SELECT q FROM QuizQuestion q " +
            "WHERE q.lesson.id = :lessonId " +
            "ORDER BY q.createdAt ASC")
    List<QuizQuestion> findQuestionsByLessonId(@Param("lessonId") String lessonId);
}
