package com.nexblog.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.article.ArticleDetailResponse;
import com.nexblog.backend.dto.article.ArticleListItemResponse;
import com.nexblog.backend.dto.article.ArticleQueryRequest;
import com.nexblog.backend.service.ArticleService;

@RestController
@RequestMapping("/api/public/articles")
public class PublicArticleController {

    private final ArticleService articleService;

    public PublicArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<Page<ArticleListItemResponse>> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String keyword
    ) {
        /*
         * 说明：
         * - 这里不做业务逻辑，只拼装查询参数并调用 service
         * - 统一返回 ApiResponse，方便前端固定解析 code/message/data
         */
        ArticleQueryRequest request = new ArticleQueryRequest(page, size, keyword);
        return ApiResponse.success(articleService.listPublicArticles(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> detail(@PathVariable Long id) {
        /*
         * 说明：
         * - 详情页只关心 id
         * - 找不到文章的异常，建议在全局异常处理器里统一转换成 404
         */
        return ApiResponse.success(articleService.getPublicArticleDetail(id));
    }
}
