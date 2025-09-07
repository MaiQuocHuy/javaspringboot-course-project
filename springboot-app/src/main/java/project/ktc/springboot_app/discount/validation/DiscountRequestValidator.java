package project.ktc.springboot_app.discount.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import project.ktc.springboot_app.discount.dto.CreateDiscountRequest;
import project.ktc.springboot_app.discount.enums.DiscountType;

/**
 * Custom validator for discount request business rules
 */
public class DiscountRequestValidator implements ConstraintValidator<ValidDiscountRequest, CreateDiscountRequest> {

    @Override
    public void initialize(ValidDiscountRequest constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CreateDiscountRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let other validations handle null
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // Rule 1: For GENERAL discounts, ownerUserId must be null
        if (request.getType() == DiscountType.GENERAL && request.getOwnerUserId() != null) {
            context.buildConstraintViolationWithTemplate(
                    "Owner user ID must be null for GENERAL discount type")
                    .addPropertyNode("ownerUserId")
                    .addConstraintViolation();
            isValid = false;
        }

        // Rule 2: For REFERRAL discounts, ownerUserId is required
        if (request.getType() == DiscountType.REFERRAL &&
                (request.getOwnerUserId() == null || request.getOwnerUserId().trim().isEmpty())) {
            context.buildConstraintViolationWithTemplate(
                    "Owner user ID is required for REFERRAL discount type")
                    .addPropertyNode("ownerUserId")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
