package project.ktc.springboot_app.comment.repositories;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

	// =================== READ OPERATIONS ===================

	/** Find all comments for a lesson ordered by lft (flattened tree structure) */
	@Query("""
			SELECT c FROM Comment c
			JOIN FETCH c.user u
			WHERE c.lesson.id = :lessonId
			AND c.isDeleted = false
			ORDER BY c.lft ASC
			""")
	List<Comment> findAllByLessonIdOrderByLft(@Param("lessonId") String lessonId);

	/** Find root comments with pagination */
	@Query("""
			SELECT c FROM Comment c
			JOIN FETCH c.user u
			WHERE c.lesson.id = :lessonId
			AND c.isDeleted = false
			AND c.parent IS NULL
			ORDER BY c.createdAt DESC
			""")
	Page<Comment> findRootCommentsByLessonId(@Param("lessonId") String lessonId, Pageable pageable);

	/** Find subtree of a comment (all descendants) */
	@Query("""
			SELECT c FROM Comment c
			JOIN FETCH c.user u
			WHERE c.lesson.id = :lessonId
			AND c.lft > :parentLft
			AND c.rgt < :parentRgt
			AND c.isDeleted = false
			ORDER BY c.lft ASC
			""")
	List<Comment> findSubtreeByParentLftRgt(
			@Param("lessonId") String lessonId,
			@Param("parentLft") Integer parentLft,
			@Param("parentRgt") Integer parentRgt);

	/** Find comment by ID with user eagerly loaded */
	@Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id")
	Optional<Comment> findByIdWithUser(@Param("id") String id);

	/** Find comment by ID with lesson and user eagerly loaded */
	@Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.lesson WHERE c.id = :id")
	Optional<Comment> findByIdWithUserAndLesson(@Param("id") String id);

	/** Count total comments for a lesson */
	@Query("SELECT COUNT(c) FROM Comment c WHERE c.lesson.id = :lessonId AND c.isDeleted = false")
	Long countByLessonId(@Param("lessonId") String lessonId);

	// =================== WRITE OPERATIONS ===================

	/** Lock parent comment for insertion (prevents race conditions) */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM Comment c WHERE c.id = :parentId")
	Optional<Comment> findByIdForUpdate(@Param("parentId") String parentId);

	/** Get maximum right value for a lesson */
	@Query("SELECT COALESCE(MAX(c.rgt), 0) FROM Comment c WHERE c.lesson.id = :lessonId")
	Integer findMaxRgtByLessonId(@Param("lessonId") String lessonId);

	/** Update left values for insertion */
	@Modifying
	@Query("""
			UPDATE Comment c
			SET c.lft = c.lft + 2
			WHERE c.lesson.id = :lessonId
			AND c.lft > :insertPosition
			""")
	int shiftLeftValuesForInsertion(
			@Param("lessonId") String lessonId, @Param("insertPosition") Integer insertPosition);

	/** Update right values for insertion */
	@Modifying
	@Query("""
			UPDATE Comment c
			SET c.rgt = c.rgt + 2
			WHERE c.lesson.id = :lessonId
			AND c.rgt > :insertPosition
			""")
	int shiftRightValuesForInsertion(
			@Param("lessonId") String lessonId, @Param("insertPosition") Integer insertPosition);

	/** Mark subtree as deleted */
	@Modifying
	@Query("""
			UPDATE Comment c
			SET c.isDeleted = true, c.content = '[Deleted]'
			WHERE c.lesson.id = :lessonId
			AND c.lft >= :subtreeLft
			AND c.rgt <= :subtreeRgt
			""")
	int markSubtreeAsDeleted(
			@Param("lessonId") String lessonId,
			@Param("subtreeLft") Integer subtreeLft,
			@Param("subtreeRgt") Integer subtreeRgt);
}
