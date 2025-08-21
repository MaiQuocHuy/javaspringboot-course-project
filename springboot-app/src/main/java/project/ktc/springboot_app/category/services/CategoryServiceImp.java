package project.ktc.springboot_app.category.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.category.dto.CategoryRequestDto;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.category.interfaces.CategoryService;
import project.ktc.springboot_app.category.repositories.CategoryRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.utils.StringUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> findAll() {
        log.info("Retrieving all categories with course count");

        List<CategoryRepository.CategoryProjection> projections = categoryRepository.findAllWithCourseCount();

        List<CategoryResponseDto> categories = projections.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} categories", categories.size());
        return ApiResponseUtil.success(categories, "Categories retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<Page<CategoryResponseDto>>> getAllCategories(String search, Pageable pageable) {
        log.info("Retrieving categories with pagination - search: {}, page: {}, size: {}",
                search, pageable.getPageNumber(), pageable.getPageSize());

        Page<Category> categoryPage = categoryRepository.findCategoriesWithSearch(search, pageable);

        Page<CategoryResponseDto> categoryResponsePage = categoryPage.map(this::mapToFullResponseDto);

        log.info("Retrieved {} total categories matching search", categoryPage.getTotalElements());
        return ApiResponseUtil.success(categoryResponsePage, "Categories retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(String id) {
        log.info("Retrieving category by id: {}", id);

        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            log.warn("Category not found with id: {}", id);
            return ApiResponseUtil.notFound("Category not found");
        }

        Category category = categoryOpt.get();
        CategoryResponseDto responseDto = mapToFullResponseDto(category);

        log.info("Successfully retrieved category: {}", category.getName());
        return ApiResponseUtil.success(responseDto, "Category retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(CategoryRequestDto requestDto) {
        log.info("Creating new category with name: {}", requestDto.getName());

        // Check if category with same name already exists
        if (categoryRepository.existsByNameIgnoreCase(requestDto.getName().trim())) {
            log.warn("Category with name '{}' already exists", requestDto.getName());
            return ApiResponseUtil.badRequest("Category with this name already exists");
        }

        Category category = new Category();
        category.setName(requestDto.getName().trim());
        category.setDescription(requestDto.getDescription() != null ? requestDto.getDescription().trim() : null);

        Category savedCategory = categoryRepository.save(category);
        CategoryResponseDto responseDto = mapToFullResponseDto(savedCategory);

        log.info("Successfully created category with id: {}", savedCategory.getId());
        return ApiResponseUtil.success(responseDto, "Category created successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(String id, CategoryRequestDto requestDto) {
        log.info("Updating category with id: {}", id);

        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            log.warn("Category not found with id: {}", id);
            return ApiResponseUtil.notFound("Category not found");
        }

        Category category = categoryOpt.get();
        String newName = requestDto.getName().trim();

        // Check if another category with the same name exists (excluding current
        // category)
        if (!category.getName().equalsIgnoreCase(newName) &&
                categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
            log.warn("Another category with name '{}' already exists", newName);
            return ApiResponseUtil.badRequest("Category with this name already exists");
        }

        category.setName(newName);
        category.setDescription(requestDto.getDescription() != null ? requestDto.getDescription().trim() : null);

        Category updatedCategory = categoryRepository.save(category);
        CategoryResponseDto responseDto = mapToFullResponseDto(updatedCategory);

        log.info("Successfully updated category: {}", updatedCategory.getName());
        return ApiResponseUtil.success(responseDto, "Category updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteCategory(String id) {
        log.info("Deleting category with id: {}", id);

        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isEmpty()) {
            log.warn("Category not found with id: {}", id);
            return ApiResponseUtil.notFound("Category not found");
        }

        Category category = categoryOpt.get();

        // Check if category has associated courses
        if (category.getCourses() != null && !category.getCourses().isEmpty()) {
            log.warn("Cannot delete category '{}' as it has {} associated courses",
                    category.getName(), category.getCourses().size());
            return ApiResponseUtil.badRequest("Cannot delete category with associated courses");
        }

        categoryRepository.delete(category);
        log.info("Successfully deleted category: {}", category.getName());

        return ApiResponseUtil.success(null, "Category deleted successfully");
    }

    /**
     * Maps CategoryProjection to CategoryResponseDto (for findAll method)
     */
    private CategoryResponseDto mapToResponseDto(CategoryRepository.CategoryProjection projection) {
        return CategoryResponseDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .slug(StringUtil.generateSlug(projection.getName()))
                .courseCount(projection.getCourseCount())
                .build();
    }

    /**
     * Maps Category entity to CategoryResponseDto (for other methods)
     */
    private CategoryResponseDto mapToFullResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(StringUtil.generateSlug(category.getName()))
                .courseCount(category.getCourses() != null ? (long) category.getCourses().size() : 0L)
                .build();
    }
}
