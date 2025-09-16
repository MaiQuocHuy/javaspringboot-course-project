package project.ktc.springboot_app.notification.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Find all notifications for a specific user
     * 
     * @param userId the user ID
     * @return list of notifications
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    boolean existsByIdAndUserId(String id, String userId);

    /**
     * Find unread notifications for a specific user
     * 
     * @param userId the user ID
     * @return list of unread notifications
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    /**
     * Count unread notifications for a specific user
     * 
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countByUserIdAndIsReadFalse(String userId);

    /**
     * Find expired notifications
     * 
     * @param currentTime current timestamp
     * @return list of expired notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.expiredAt IS NOT NULL AND n.expiredAt < :currentTime")
    List<Notification> findExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);
}
