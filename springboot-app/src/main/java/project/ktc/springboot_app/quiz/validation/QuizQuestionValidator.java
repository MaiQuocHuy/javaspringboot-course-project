package project.ktc.springboot_app.quiz.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Set;

public class QuizQuestionValidator implements ConstraintValidator<ValidQuizQuestion, Object> {

  @Override
  public void initialize(ValidQuizQuestion constraintAnnotation) {
    // No initialization needed
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isValid(Object question, ConstraintValidatorContext context) {
    if (question == null) {
      return true; // Let @NotNull handle null validation
    }

    // Extract data using reflection or common interface
    Map<String, String> options = null;
    String correctAnswer = null;
    String questionText = null;

    try {
      // Use reflection to get fields from both CreateQuizQuestionDto and
      // UpdateQuizQuestionDto
      options = (Map<String, String>) question.getClass().getMethod("getOptions").invoke(question);
      correctAnswer = (String) question.getClass().getMethod("getCorrectAnswer").invoke(question);
      questionText = (String) question.getClass().getMethod("getQuestionText").invoke(question);
    } catch (Exception e) {
      // If reflection fails, return false
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Unable to validate quiz question structure")
          .addConstraintViolation();
      return false;
    }

    // Disable default constraint violation
    context.disableDefaultConstraintViolation();

    boolean isValid = true;

    // Validate question text
    if (questionText == null || questionText.trim().isEmpty()) {
      context
          .buildConstraintViolationWithTemplate("Question text is required")
          .addPropertyNode("questionText")
          .addConstraintViolation();
      isValid = false;
    }

    // Validate options contain exactly A, B, C, D
    if (options == null || options.size() != 4) {
      context
          .buildConstraintViolationWithTemplate("Must have exactly 4 options (A, B, C, D)")
          .addPropertyNode("options")
          .addConstraintViolation();
      isValid = false;
    } else {
      Set<String> requiredKeys = Set.of("A", "B", "C", "D");
      if (!options.keySet().equals(requiredKeys)) {
        context
            .buildConstraintViolationWithTemplate("Options must be exactly A, B, C, D")
            .addPropertyNode("options")
            .addConstraintViolation();
        isValid = false;
      }

      // Check if any option value is empty
      for (Map.Entry<String, String> entry : options.entrySet()) {
        if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
          context
              .buildConstraintViolationWithTemplate("Option " + entry.getKey() + " cannot be empty")
              .addPropertyNode("options")
              .addConstraintViolation();
          isValid = false;
        }
      }
    }

    // Validate correct answer
    if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
      context
          .buildConstraintViolationWithTemplate("Correct answer is required")
          .addPropertyNode("correctAnswer")
          .addConstraintViolation();
      isValid = false;
    } else if (options != null && !options.containsKey(correctAnswer)) {
      context
          .buildConstraintViolationWithTemplate(
              "Correct answer '" + correctAnswer + "' must be one of the provided options")
          .addPropertyNode("correctAnswer")
          .addConstraintViolation();
      isValid = false;
    } else if (!correctAnswer.matches("^[ABCD]$")) {
      context
          .buildConstraintViolationWithTemplate("Correct answer must be A, B, C, or D")
          .addPropertyNode("correctAnswer")
          .addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }
}
