package project.ktc.springboot_app.comment.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.comment.entity.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    /**
     * Find paginated root comments for a lesson (no parent)
     * Ordered by creation date DESC (newest first)
     */
    @Query("SELECT c FROM Comment c " +
            "WHERE c.lesson.id = :lessonId " +
            "AND c.parent IS NULL " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findRootCommentsByLessonId(@Param("lessonId") String lessonId, Pageable pageable);

    /**
     * Find all replies for specific parent comments
     * Ordered by creation date ASC (oldest first)
     */
    @Query("SELECT c FROM Comment c " +
            "WHERE c.parent.id IN :parentIds " +
            "ORDER BY c.parent.id, c.createdAt ASC")
    List<Comment> findRepliesByParentIds(@Param("parentIds") List<String> parentIds);

    /**
     * Find all comments in a thread including replies
     * Used for fetching complete comment trees
     */
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE c.lesson.id = :lessonId " +
            "AND (c.parent IS NULL OR c.parent.id IN :rootCommentIds) " +
            "ORDER BY c.parent.id NULLS FIRST, c.createdAt ASC")
    List<Comment> findCommentsWithRepliesByLessonAndRootIds(
            @Param("lessonId") String lessonId,
            @Param("rootCommentIds") List<String> rootCommentIds);

    /**
     * Find comment by ID with user eagerly loaded
     */
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.lesson " +
            "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithUser(@Param("commentId") String commentId);

    /**
     * Count total comments for a lesson (including replies)
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.lesson.id = :lessonId AND c.isDeleted = false")
    Long countByLessonId(@Param("lessonId") String lessonId);

    /**
     * Count replies for specific parent comment
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :parentId AND c.isDeleted = false")
    Long countRepliesByParentId(@Param("parentId") String parentId);

    /**
     * Check if user can add reply based on depth limit
     */
    @Query("SELECT c.depth FROM Comment c WHERE c.id = :commentId")
    Optional<Integer> findDepthById(@Param("commentId") String commentId);

    /**
     * Soft delete comment and update content
     */
    @Query("UPDATE Comment c SET c.isDeleted = true, c.content = '[Deleted]' WHERE c.id = :commentId")
    void softDeleteComment(@Param("commentId") String commentId);

    /**
     * Check if comment exists and is not deleted
     */
    boolean existsByIdAndIsDeletedFalse(String commentId);

    /**
     * Find all descendants of a comment (for admin operations)
     */
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId OR c.parent.parent.id = :parentId")
    List<Comment> findDescendantsByParentId(@Param("parentId") String parentId);

    /**
     * Find all replies in a thread including nested replies recursively
     * This gets all comments that belong to a thread started by root comments
     */
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "WHERE c.lesson.id = :lessonId " +
            "AND c.parent IS NOT NULL " +
            "AND c.isDeleted = false " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findAllRepliesInThread(@Param("lessonId") String lessonId, @Param("rootCommentIds") List<String> rootCommentIds);
}
