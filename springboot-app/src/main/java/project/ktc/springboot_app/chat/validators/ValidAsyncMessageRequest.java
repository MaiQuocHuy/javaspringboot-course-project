package project.ktc.springboot_app.chat.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AsyncMessageRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAsyncMessageRequest {
  String message() default "Invalid async message request";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
