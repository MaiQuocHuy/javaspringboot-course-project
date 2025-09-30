package project.ktc.springboot_app.category.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.category.dto.CategoryRequestDto;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.interfaces.CategoryService;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Admin Category Management",
    description = "APIs for managing course categories (Admin only)")
public class AdminCategoryController {
  private final CategoryService categoryService;

  @Operation(
      summary = "Get categories with pagination",
      description = "Retrieve categories with pagination and optional search functionality")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
      })
  @GetMapping
  @PreAuthorize("hasPermission('Category', 'category:READ')")
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<Page<CategoryResponseDto>>>
      getAllCategories(
          @Parameter(
                  description = "Search term for category name or description",
                  example = "programming")
              @RequestParam(required = false)
              String search,
          @Parameter(description = "Page number (0-based)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "Number of items per page", example = "10")
              @RequestParam(defaultValue = "10")
              int size,
          @Parameter(description = "Sort field", example = "createdAt")
              @RequestParam(defaultValue = "createdAt")
              String sortBy,
          @Parameter(description = "Sort direction", example = "desc")
              @RequestParam(defaultValue = "desc")
              String sortDir) {

    log.info(
        "Admin requesting categories - page: {}, size: {}, search: {}, sortBy: {}, sortDir: {}",
        page,
        size,
        search,
        sortBy,
        sortDir);

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    return categoryService.getAllCategories(search, pageable);
  }

  @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
      })
  @GetMapping("/{id}")
  @PreAuthorize("hasPermission('Category', 'category:READ')")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CategoryResponseDto>>
      getCategoryById(
          @Parameter(description = "Category ID", required = true) @PathVariable String id) {

    log.info("Admin requesting category with id: {}", id);
    return categoryService.getCategoryById(id);
  }

  @Operation(summary = "Create new category", description = "Create a new course category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category data or category name already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
      })
  @PostMapping
  @PreAuthorize("hasPermission('Category', 'category:CREATE')")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CategoryResponseDto>>
      createCategory(
          @Parameter(description = "Category data", required = true) @Valid @RequestBody
              CategoryRequestDto requestDto) {

    log.info("Admin creating new category: {}", requestDto.getName());
    return categoryService.createCategory(requestDto);
  }

  @Operation(summary = "Update category", description = "Update an existing category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid category data or category name already exists"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
      })
  @PutMapping("/{id}")
  @PreAuthorize("hasPermission('Category', 'category:UPDATE')")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CategoryResponseDto>>
      updateCategory(
          @Parameter(description = "Category ID", required = true) @PathVariable String id,
          @Parameter(description = "Updated category data", required = true) @Valid @RequestBody
              CategoryRequestDto requestDto) {

    log.info("Admin updating category with id: {} to name: {}", id, requestDto.getName());
    return categoryService.updateCategory(id, requestDto);
  }

  @Operation(
      summary = "Delete category",
      description = "Delete a category (only if it has no associated courses)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete category with associated courses"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasPermission('Category', 'category:DELETE')")
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteCategory(
      @Parameter(description = "Category ID", required = true) @PathVariable String id) {

    log.info("Admin deleting category with id: {}", id);
    return categoryService.deleteCategory(id);
  }
}
