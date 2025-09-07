package project.ktc.springboot_app.discount.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for discount requests
 * Validates business rules specific to discount creation
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DiscountRequestValidator.class)
@Documented
public @interface ValidDiscountRequest {
    String message() default "Invalid discount request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
