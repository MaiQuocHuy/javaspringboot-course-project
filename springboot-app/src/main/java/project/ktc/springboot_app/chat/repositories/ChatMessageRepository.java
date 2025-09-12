package project.ktc.springboot_app.chat.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import project.ktc.springboot_app.chat.entities.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

        @Query("SELECT m FROM ChatMessage m JOIN FETCH m.messageType mt WHERE m.course.id = :courseId ORDER BY m.createdAt DESC")
        Page<ChatMessage> findByCourseId(@Param("courseId") String courseId, Pageable pageable);

        @Query("SELECT m FROM ChatMessage m JOIN FETCH m.messageType mt WHERE m.course.id = :courseId AND mt.name = :type ORDER BY m.createdAt DESC")
        Page<ChatMessage> findByCourseIdAndType(@Param("courseId") String courseId, @Param("type") String type,
                        Pageable pageable);

        // Methods for keyset pagination (infinite scroll)
        // Note: Since our entity uses String IDs (UUIDs), we need to convert for the
        // API

        @Query("SELECT m FROM ChatMessage m " +
                        "LEFT JOIN FETCH m.textDetail " +
                        "LEFT JOIN FETCH m.fileDetail " +
                        "LEFT JOIN FETCH m.audioDetail " +
                        "LEFT JOIN FETCH m.videoDetail " +
                        "JOIN FETCH m.messageType mt " +
                        "JOIN FETCH m.sender " +
                        "WHERE m.course.id = :courseId " +
                        "ORDER BY m.createdAt DESC")
        List<ChatMessage> findByCourseIdOrderByCreatedAtAsc(@Param("courseId") String courseId, Pageable pageable);

        @Query("SELECT m FROM ChatMessage m " +
                        "LEFT JOIN FETCH m.textDetail " +
                        "LEFT JOIN FETCH m.fileDetail " +
                        "LEFT JOIN FETCH m.audioDetail " +
                        "LEFT JOIN FETCH m.videoDetail " +
                        "JOIN FETCH m.messageType mt " +
                        "JOIN FETCH m.sender " +
                        "WHERE m.course.id = :courseId AND m.createdAt < " +
                        "(SELECT m2.createdAt FROM ChatMessage m2 WHERE m2.id = :beforeMessageId) " +
                        "ORDER BY m.createdAt DESC")
        List<ChatMessage> findByCourseIdBeforeMessageId(@Param("courseId") String courseId,
                        @Param("beforeMessageId") String beforeMessageId,
                        Pageable pageable);

        @Query("SELECT m FROM ChatMessage m " +
                        "LEFT JOIN FETCH m.textDetail " +
                        "LEFT JOIN FETCH m.fileDetail " +
                        "LEFT JOIN FETCH m.audioDetail " +
                        "LEFT JOIN FETCH m.videoDetail " +
                        "JOIN FETCH m.messageType mt " +
                        "JOIN FETCH m.sender " +
                        "WHERE m.course.id = :courseId AND m.createdAt > " +
                        "(SELECT m2.createdAt FROM ChatMessage m2 WHERE m2.id = :afterMessageId) " +
                        "ORDER BY m.createdAt DESC")
        List<ChatMessage> findByCourseIdAfterMessageId(@Param("courseId") String courseId,
                        @Param("afterMessageId") String afterMessageId,
                        Pageable pageable);

        @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.course.id = :courseId")
        Long countByCourseId(@Param("courseId") String courseId);
}
