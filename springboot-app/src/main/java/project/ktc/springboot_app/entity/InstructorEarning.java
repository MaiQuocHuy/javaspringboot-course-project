package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "INSTRUCTOR_EARNING", uniqueConstraints = @UniqueConstraint(name = "unique_payment", columnNames = {"payment_id"}), indexes = {
        @Index(name = "idx_instructor", columnList = "instructor_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
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

    @Column(nullable = false)
    private String status;

    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
} 