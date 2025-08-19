package project.ktc.springboot_app.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.refund.entity.Refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_user", columnList = "user_id"),
        @Index(name = "idx_pay_course", columnList = "course_id"),
        @Index(name = "idx_pay_status", columnList = "status"),
        @Index(name = "idx_pay_session_id", columnList = "session_id")
})
@Getter
@Setter
public class Payment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "session_id", columnDefinition = "TEXT")
    private String sessionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refund> refunds;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstructorEarning> instructorEarnings;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}