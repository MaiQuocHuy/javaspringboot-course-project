package project.ktc.springboot_app.quiz.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project.ktc.springboot_app.quiz.dto.CreateQuizQuestionDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizQuestionDto;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuizQuestionValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreateQuizQuestionDto() {
        CreateQuizQuestionDto dto = CreateQuizQuestionDto.builder()
                .questionText("What is Spring Boot?")
                .options(Map.of(
                        "A", "A framework",
                        "B", "A library",
                        "C", "A language",
                        "D", "A database"))
                .correctAnswer("A")
                .explanation("Spring Boot is a Java framework")
                .build();

        Set<ConstraintViolation<CreateQuizQuestionDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid CreateQuizQuestionDto should have no violations");
    }

    @Test
    void testValidUpdateQuizQuestionDto() {
        UpdateQuizQuestionDto dto = UpdateQuizQuestionDto.builder()
                .id("question-id")
                .questionText("What is Spring Boot?")
                .options(Map.of(
                        "A", "A framework",
                        "B", "A library",
                        "C", "A language",
                        "D", "A database"))
                .correctAnswer("A")
                .explanation("Spring Boot is a Java framework")
                .build();

        Set<ConstraintViolation<UpdateQuizQuestionDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid UpdateQuizQuestionDto should have no violations");
    }

    @Test
    void testInvalidCreateQuizQuestionDto_MissingOption() {
        CreateQuizQuestionDto dto = CreateQuizQuestionDto.builder()
                .questionText("What is Spring Boot?")
                .options(Map.of(
                        "A", "A framework",
                        "B", "A library",
                        "C", "A language"
                // Missing option D
                ))
                .correctAnswer("A")
                .build();

        Set<ConstraintViolation<CreateQuizQuestionDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Invalid CreateQuizQuestionDto should have violations");

        boolean hasOptionsViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("options"));
        assertTrue(hasOptionsViolation, "Should have violation for options");
    }

    @Test
    void testInvalidUpdateQuizQuestionDto_WrongCorrectAnswer() {
        UpdateQuizQuestionDto dto = UpdateQuizQuestionDto.builder()
                .id("question-id")
                .questionText("What is Spring Boot?")
                .options(Map.of(
                        "A", "A framework",
                        "B", "A library",
                        "C", "A language",
                        "D", "A database"))
                .correctAnswer("E") // Invalid correct answer
                .build();

        Set<ConstraintViolation<UpdateQuizQuestionDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Invalid UpdateQuizQuestionDto should have violations");

        boolean hasCorrectAnswerViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("correctAnswer"));
        assertTrue(hasCorrectAnswerViolation, "Should have violation for correctAnswer");
    }

    @Test
    void testInvalidOptions_EmptyValue() {
        CreateQuizQuestionDto dto = CreateQuizQuestionDto.builder()
                .questionText("What is Spring Boot?")
                .options(Map.of(
                        "A", "A framework",
                        "B", "", // Empty option value
                        "C", "A language",
                        "D", "A database"))
                .correctAnswer("A")
                .build();

        Set<ConstraintViolation<CreateQuizQuestionDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Should have violations for empty option value");
    }
}
