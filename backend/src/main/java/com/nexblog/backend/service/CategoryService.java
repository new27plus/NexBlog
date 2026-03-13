package com.nexblog.backend.service;

import org.springframework.data.domain.Page;

import com.nexblog.backend.dto.category.CategoryCreateRequest;
import com.nexblog.backend.dto.category.CategoryDetailResponse;
import com.nexblog.backend.dto.category.CategoryListItemResponse;
import com.nexblog.backend.dto.category.CategoryQueryRequest;
import com.nexblog.backend.dto.category.CategoryUpdateRequest;

public interface CategoryService {

    /*
     * 分页获取分类列表
     */
    Page<CategoryListItemResponse> listPublicCategories(CategoryQueryRequest request);

    CategoryDetailResponse getPublicCategoryDetail(Long id);

    CategoryDetailResponse createCategory(CategoryCreateRequest request);

    CategoryDetailResponse updateCategory(Long id, CategoryUpdateRequest request);

    void deleteCategory(Long id);
}
