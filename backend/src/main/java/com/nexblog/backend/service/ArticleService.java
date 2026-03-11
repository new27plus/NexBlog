package com.nexblog.backend.service;

import org.springframework.data.domain.Page;

import com.nexblog.backend.dto.article.ArticleCreateRequest;
import com.nexblog.backend.dto.article.ArticleDetailResponse;
import com.nexblog.backend.dto.article.ArticleListItemResponse;
import com.nexblog.backend.dto.article.ArticleQueryRequest;
import com.nexblog.backend.dto.article.ArticleUpdateRequest;

public interface ArticleService {

    /*
     * 给前台首页用：分页获取文章列表
     */
    Page<ArticleListItemResponse> listPublicArticles(ArticleQueryRequest request);

    /*
     * 给前台详情页用：根据 id 获取文章详情
     */
    ArticleDetailResponse getPublicArticleDetail(Long id);

    /*
     * 给后台管理用：创建文章
     */
    ArticleDetailResponse createArticle(ArticleCreateRequest request);

    /*
     * 给后台管理用：更新文章
     */
    ArticleDetailResponse updateArticle(Long id, ArticleUpdateRequest request);

    /*
     * 给后台管理用：删除文章
     */
    void deleteArticle(Long id);
}
