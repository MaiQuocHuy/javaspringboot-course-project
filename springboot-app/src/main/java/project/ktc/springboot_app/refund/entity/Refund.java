package project.ktc.springboot_app.refund.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import project.ktc.springboot_app.payment.entity.Payment;

@Entity
@Table(
    name = "refunds",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_refund_payment",
            columnNames = {"payment_id"}),
    indexes = {@Index(name = "idx_refund_status", columnList = "status")})
@Getter
@Setter
public class Refund {

  @Id
  @Column(length = 36, updatable = false, nullable = false)
  private String id = UUID.randomUUID().toString();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RefundStatus status = RefundStatus.PENDING;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @Column(columnDefinition = "TEXT", nullable = true)
  private String rejectedReason;

  @CreationTimestamp
  @Column(name = "requested_at", updatable = false)
  private LocalDateTime requestedAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  public enum RefundStatus {
    PENDING,
    COMPLETED,
    FAILED
  }
}
