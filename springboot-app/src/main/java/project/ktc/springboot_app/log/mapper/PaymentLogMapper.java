package project.ktc.springboot_app.log.mapper;

import project.ktc.springboot_app.log.dto.PaymentLogDto;
import project.ktc.springboot_app.payment.entity.Payment;

/**
 * Mapper utility for converting Payment entity to PaymentLogDto for audit
 * logging
 */
public class PaymentLogMapper {

    /**
     * Converts Payment entity to PaymentLogDto for system logging
     */
    public static PaymentLogDto toLogDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentLogDto.PaymentLogDtoBuilder builder = PaymentLogDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentMethod(payment.getPaymentMethod())
                .sessionId(payment.getSessionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt());

        // Add user information if available
        if (payment.getUser() != null) {
            builder.userId(payment.getUser().getId())
                    .user(PaymentLogDto.UserLogDto.builder()
                            .id(payment.getUser().getId())
                            .name(payment.getUser().getName())
                            .email(payment.getUser().getEmail())
                            .build());
        }

        // Add course information if available
        if (payment.getCourse() != null) {
            PaymentLogDto.CourseLogDto.CourseLogDtoBuilder courseBuilder = PaymentLogDto.CourseLogDto.builder()
                    .id(payment.getCourse().getId())
                    .title(payment.getCourse().getTitle())
                    .price(payment.getCourse().getPrice());

            // Add instructor information if available
            if (payment.getCourse().getInstructor() != null) {
                courseBuilder.instructorId(payment.getCourse().getInstructor().getId())
                        .instructorName(payment.getCourse().getInstructor().getName());
            }

            builder.courseId(payment.getCourse().getId())
                    .course(courseBuilder.build());
        }

        return builder.build();
    }
}
