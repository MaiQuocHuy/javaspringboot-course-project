package project.ktc.springboot_app.category.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.category.interfaces.CategoryService;
import project.ktc.springboot_app.category.repositories.CategoryRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.utils.StringUtil;

import java.util.List;
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

    /**
     * Maps CategoryProjection to CategoryResponseDto
     */
    private CategoryResponseDto mapToResponseDto(CategoryRepository.CategoryProjection projection) {
        return CategoryResponseDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .slug(StringUtil.generateSlug(projection.getName()))
                .courseCount(projection.getCourseCount())
                .build();
    }
}
