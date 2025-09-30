package project.ktc.springboot_app.category.interfaces;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.category.dto.CategoryRequestDto;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.common.dto.ApiResponse;

public interface CategoryService {

	// Create new category
	ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(CategoryRequestDto requestDto);

	// Get all categories with pagination and search
	ResponseEntity<ApiResponse<Page<CategoryResponseDto>>> getAllCategories(
			String search, Pageable pageable);

	// Get all categories for dropdown (with course count)
	ResponseEntity<ApiResponse<List<CategoryResponseDto>>> findAll();

	// Get category by ID
	ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(String id);

	// Update category
	ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
			String id, CategoryRequestDto requestDto);

	// Delete category
	ResponseEntity<ApiResponse<Void>> deleteCategory(String id);
}
