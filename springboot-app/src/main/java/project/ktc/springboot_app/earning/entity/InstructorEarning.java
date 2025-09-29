package project.ktc.springboot_app.earning.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "instructor_earnings", uniqueConstraints = @UniqueConstraint(name = "unique_earn_payment", columnNames = {
                "payment_id" }), indexes = {
                                @Index(name = "idx_earn_instructor", columnList = "instructor_id"),
                                @Index(name = "idx_earn_status", columnList = "status")
                })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorEarning extends BaseEntity {
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "instructor_id", nullable = false)
        private User instructor;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payment_id", nullable = false)
        private Payment payment;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "course_id", nullable = false)
        private Course course;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private EarningStatus status = EarningStatus.PENDING;

        @Column(name = "paid_at")
        private LocalDateTime paidAt;

        public enum EarningStatus {
                PENDING, AVAILABLE, PAID
        }
}