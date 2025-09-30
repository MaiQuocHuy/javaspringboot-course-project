package project.ktc.springboot_app.category.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.interfaces.CategoryService;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories API", description = "Endpoints for managing categories")
@Validated
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(
      summary = "Get all categories",
      description =
          "Retrieves a list of all course categories with course count. "
              + "Categories with zero courses are still included in the result. "
              + "This endpoint is publicly accessible and does not require authentication.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<List<CategoryResponseDto>>>
      findAll() {
    log.info("GET /api/categories - Retrieving all categories");
    return categoryService.findAll();
  }
}
