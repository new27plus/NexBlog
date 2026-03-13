package com.nexblog.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.category.CategoryCreateRequest;
import com.nexblog.backend.dto.category.CategoryDetailResponse;
import com.nexblog.backend.dto.category.CategoryListItemResponse;
import com.nexblog.backend.dto.category.CategoryQueryRequest;
import com.nexblog.backend.dto.category.CategoryUpdateRequest;
import com.nexblog.backend.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/studio/categories")
public class StudioCategoryController {

    private final CategoryService categoryService;

    public StudioCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<Page<CategoryListItemResponse>> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(categoryService.listPublicCategories(new CategoryQueryRequest(page, size, keyword)));
    }
    

    @GetMapping("/{id}")
    public ApiResponse<CategoryDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getPublicCategoryDetail(id));
    }

    @PostMapping
    public ApiResponse<CategoryDetailResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }
    
    @PutMapping("/{id}")
    public ApiResponse<CategoryDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return ApiResponse.success(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
