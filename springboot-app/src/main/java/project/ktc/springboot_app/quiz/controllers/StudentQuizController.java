package project.ktc.springboot_app.quiz.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.quiz.dto.QuizScoreResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizScoreDetailResponseDto;
import project.ktc.springboot_app.quiz.services.StudentQuizServiceImp;

@Tag(name = "Student Quiz", description = "Student quiz management APIs")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentQuizController {

    private final StudentQuizServiceImp studentQuizService;

    @Operation(summary = "Get quiz scores", description = "Retrieves the quiz scores for all completed quizzes in courses the student is enrolled in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz scores retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @GetMapping("/quiz-score")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<QuizScoreResponseDto>>> getQuizScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "completedAt,desc") String sort) {

        log.info("Getting quiz scores for current user - page: {}, size: {}, sort: {}", page, size, sort);

        // Parse sort parameter
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        return studentQuizService.getQuizScores(pageable);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "completedAt");
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, property);
    }

    @Operation(summary = "Get quiz score details", description = "Retrieves detailed information about a specific quiz score for the currently authenticated student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz score details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Quiz result not found or access denied")
    })
    @GetMapping("/quiz-score/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<QuizScoreDetailResponseDto>> getQuizScoreDetail(
            @PathVariable String id) {

        log.info("Getting quiz score detail for quiz result ID: {}", id);

        return studentQuizService.getQuizScoreDetail(id);
    }
}
