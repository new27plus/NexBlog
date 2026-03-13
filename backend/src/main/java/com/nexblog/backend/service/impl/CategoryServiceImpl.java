package com.nexblog.backend.service.impl;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.nexblog.backend.dto.category.CategoryCreateRequest;
import com.nexblog.backend.dto.category.CategoryDetailResponse;
import com.nexblog.backend.dto.category.CategoryListItemResponse;
import com.nexblog.backend.dto.category.CategoryQueryRequest;
import com.nexblog.backend.dto.category.CategoryUpdateRequest;
import com.nexblog.backend.entity.Category;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.CategoryRepository;
import com.nexblog.backend.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryListItemResponse> listPublicCategories(CategoryQueryRequest request) {
        int page = request.page() == null || request.page() < 0 ? 0 : request.page();
        int size = request.size() == null || request.size() <= 0 ? 10 : Math.min(request.size(), 50);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());

        Page<Category> categoryPage;
        if (!StringUtils.hasText(request.keyword())) {
            categoryPage = categoryRepository.findAll(pageable);
        } else {
            categoryPage = categoryRepository.findByNameContainingIgnoreCase(request.keyword().trim(), pageable);
        }

        return categoryPage.map(this::toListItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getPublicCategoryDetail(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new BusinessException(10002, "分类不存在，id=" + id));
        return toDetailResponse(category);
    }

    @Override
    @Transactional
    public CategoryDetailResponse createCategory(CategoryCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category category = new Category();
        category.setName(request.name().trim());
        category.setCreateTime(now);
        category.setUpDateTime(now);

        Category saved = categoryRepository.save(category);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public CategoryDetailResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new BusinessException(10002, "分类不存在，id=" + id));

        category.setName(request.name().trim());
        category.setUpDateTime(LocalDateTime.now());

        Category updated = categoryRepository.save(category);
        return toDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BusinessException(10002, "分类不存在，id=" + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryListItemResponse toListItemResponse(Category category) {
        return new CategoryListItemResponse(
                category.getId(),
                category.getName(),
                category.getCreateTime(),
                category.getUpDateTime());
    }

    private CategoryDetailResponse toDetailResponse(Category category) {
        return new CategoryDetailResponse(
                category.getId(),
                category.getName(),
                category.getCreateTime(),
                category.getUpDateTime());
    }

}
