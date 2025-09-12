package project.ktc.springboot_app.quiz.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.quiz.dto.CreateQuizDto;
import project.ktc.springboot_app.quiz.dto.QuizResponseDto;
import project.ktc.springboot_app.quiz.services.QuizServiceImp;
import project.ktc.springboot_app.utils.SecurityUtil;

@RestController
@RequestMapping("/api/instructor/quizzes")
@PreAuthorize("hasRole('INSTRUCTOR')")
@Tag(name = "Instructor Quiz Management", description = "Endpoints for instructors to manage quiz content")
@RequiredArgsConstructor
@Slf4j
public class InstructorQuizControllerOld {

        private final QuizServiceImp quizService;

        @PostMapping
        @Operation(summary = "Create a new quiz for a lesson", description = "Creates a new quiz with multiple choice questions for a lesson owned by the instructor. "
                        +
                        "The lesson must be of type QUIZ and should not already have quiz questions.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Quiz created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or validation errors", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Valid authentication required", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - INSTRUCTOR role required or lesson not owned by instructor", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - Lesson already has quiz questions", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<QuizResponseDto>> createQuiz(
                        @Parameter(description = "Quiz creation data including title, description, lesson ID and questions") @Valid @RequestBody CreateQuizDto createQuizDto) {

                String currentUserId = SecurityUtil.getCurrentUserId();
                QuizResponseDto quiz = quizService.createQuiz(createQuizDto, currentUserId);
                return ApiResponseUtil.created(quiz, "Quiz created successfully");
        }
}
