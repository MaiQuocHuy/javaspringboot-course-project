package project.ktc.springboot_app.category.interfaces;

import project.ktc.springboot_app.category.dto.CategoryResponseDto;
import project.ktc.springboot_app.common.dto.ApiResponse;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface CategoryService {

    ResponseEntity<ApiResponse<List<CategoryResponseDto>>> findAll();
}
