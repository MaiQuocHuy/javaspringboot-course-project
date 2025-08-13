package project.ktc.springboot_app.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.email.dto.EmailRequest;

import java.time.LocalDateTime;

/**
 * Entity to store failed email attempts for retry
 */
@Entity
@Table(name = "failed_emails")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String emailRequestJson; // Store the entire EmailRequest as JSON

    @Enumerated(EnumType.STRING)
    private EmailRequest.EmailPriority priority;

    @Column(nullable = false)
    private String errorMessage;

    @Builder.Default
    private Integer attemptCount = 0;

    @Builder.Default
    private Integer maxAttempts = 3;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastAttemptAt;

    private LocalDateTime nextRetryAt;

    @Builder.Default
    private Boolean processed = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.attemptCount++;
    }

    /**
     * Get retry count
     */
    public Integer getRetryCount() {
        return this.attemptCount;
    }

    /**
     * Set last error message
     */
    public void setLastError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Calculate next retry time based on exponential backoff
     */
    public void calculateNextRetryAt(project.ktc.springboot_app.email.config.EmailConfig.Retry retryConfig) {
        if (retryConfig.isExponentialBackoff()) {
            long delay = (long) (retryConfig.getInitialDelay()
                    * Math.pow(retryConfig.getBackoffMultiplier(), attemptCount));
            delay = Math.min(delay, retryConfig.getMaxDelay());
            this.nextRetryAt = LocalDateTime.now().plusSeconds(delay);
        } else {
            this.nextRetryAt = LocalDateTime.now().plusSeconds(retryConfig.getInitialDelay());
        }
    }
}
