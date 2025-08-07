package project.ktc.springboot_app.course.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Max;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.services.CourseServiceImp;
import project.ktc.springboot_app.course.enums.CourseLevel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Courses API", description = "Endpoints for managing courses")
@RequiredArgsConstructor
@Validated
public class CourseController {

        private final CourseServiceImp courseService;

        @GetMapping
        @Operation(summary = "Get all published courses", description = "Retrieves a paginated list of all published and non-deleted courses with filtering and sorting options")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> findAllPublic(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,

                        @Parameter(description = "Search by course title, description, or instructor name") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by category ID") @RequestParam(required = false) String categoryId,

                        @Parameter(description = "Minimum course price") @RequestParam(required = false) BigDecimal minPrice,

                        @Parameter(description = "Maximum course price") @RequestParam(required = false) BigDecimal maxPrice,

                        @Parameter(description = "Filter by course level") @RequestParam(required = false) CourseLevel level,

                        @Parameter(description = "Sort field and direction (e.g., 'price,asc', 'title,desc')") @RequestParam(defaultValue = "createdAt,desc") String sort) {
                // Create Pageable with sorting
                Sort.Direction sortDirection = Sort.Direction.ASC;
                String sortField = "createdAt";

                if (sort != null && sort.contains(",")) {
                        String[] sortParams = sort.split(",");
                        sortField = sortParams[0];
                        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
                                sortDirection = Sort.Direction.DESC;
                        }
                }

                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

                // Call service method to get filtered courses
                ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> result = courseService
                                .findAllPublic(
                                                search, categoryId, minPrice, maxPrice, level, pageable);

                return result;
        }

        @GetMapping("/slug/{slug}")
        @Operation(summary = "Get course details by slug", description = "Retrieves detailed information about a single published course using slug including ratings, sections, lessons, and quiz counts")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course details retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseDetailResponseDto>> findOneBySlug(
                        @Parameter(description = "Course slug") @PathVariable String slug) {

                ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseDetailResponseDto>> result = courseService
                                .findOneBySlug(slug);

                return result;
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get course details by ID", description = "Retrieves detailed information about a single published course including ratings, sections, and lessons")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course details retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseDetailResponseDto>> findOnePublic(
                        @Parameter(description = "Course ID") @PathVariable String id) {

                ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseDetailResponseDto>> result = courseService
                                .findOnePublic(id);

                return result;
        }
}
