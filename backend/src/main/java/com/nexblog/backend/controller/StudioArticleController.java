package com.nexblog.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.article.ArticleCreateRequest;
import com.nexblog.backend.dto.article.ArticleDetailResponse;
import com.nexblog.backend.dto.article.ArticleListItemResponse;
import com.nexblog.backend.dto.article.ArticleQueryRequest;
import com.nexblog.backend.dto.article.ArticleUpdateRequest;
import com.nexblog.backend.service.ArticleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/studio/articles", "/api/admin/articles"})
public class StudioArticleController {

    private final ArticleService articleService;

    public StudioArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<Page<ArticleListItemResponse>> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(articleService.listPublicArticles(new ArticleQueryRequest(page, size, keyword)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(articleService.getPublicArticleDetail(id));
    }

    @PostMapping
    public ApiResponse<ArticleDetailResponse> create(@Valid @RequestBody ArticleCreateRequest request) {
        return ApiResponse.success(articleService.createArticle(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ArticleUpdateRequest request
    ) {
        return ApiResponse.success(articleService.updateArticle(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ApiResponse.success(null);
    }
}
