package project.ktc.springboot_app.quiz.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = QuizQuestionValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuizQuestion {
    String message() default "Invalid quiz question format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
