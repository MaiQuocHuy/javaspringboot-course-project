package project.ktc.springboot_app.log.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.log.entity.SystemLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SystemLog entity operations
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    /**
     * Find logs by user ID
     */
    List<SystemLog> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find logs by action type
     */
    List<SystemLog> findByActionOrderByCreatedAtDesc(SystemLog.Action action);

    /**
     * Find logs by entity type and entity ID
     */
    List<SystemLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    /**
     * Find logs within date range
     */
    @Query("SELECT sl FROM SystemLog sl WHERE sl.createdAt BETWEEN :startDate AND :endDate ORDER BY sl.createdAt DESC")
    List<SystemLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find logs by multiple criteria with pagination
     */
    @Query("SELECT sl FROM SystemLog sl WHERE " +
            "(:userId IS NULL OR sl.user.id = :userId) AND " +
            "(:action IS NULL OR sl.action = :action) AND " +
            "(:entityType IS NULL OR sl.entityType = :entityType) AND " +
            "(:startDate IS NULL OR sl.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR sl.createdAt <= :endDate) " +
            "ORDER BY sl.createdAt DESC")
    Page<SystemLog> findByCriteria(@Param("userId") String userId,
            @Param("action") SystemLog.Action action,
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
