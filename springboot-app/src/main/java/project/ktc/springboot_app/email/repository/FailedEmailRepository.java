package project.ktc.springboot_app.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.email.entity.FailedEmail;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for failed email management
 */
@Repository
public interface FailedEmailRepository extends JpaRepository<FailedEmail, String> {

    /**
     * Find failed emails that are ready for retry
     */
    @Query("SELECT f FROM FailedEmail f WHERE f.processed = false AND f.attemptCount < f.maxAttempts AND (f.nextRetryAt IS NULL OR f.nextRetryAt <= :now)")
    List<FailedEmail> findReadyForRetry(LocalDateTime now);

    /**
     * Find failed emails by processing status
     */
    List<FailedEmail> findByProcessedFalse();

    /**
     * Count unprocessed failed emails
     */
    long countByProcessedFalse();

    /**
     * Find failed emails created between dates
     */
    List<FailedEmail> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find failed emails older than specified date
     */
    List<FailedEmail> findByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Alternative method name for finding old failed emails
     */
    default List<FailedEmail> findOlderThan(LocalDateTime cutoffDate) {
        return findByCreatedAtBefore(cutoffDate);
    }
}
