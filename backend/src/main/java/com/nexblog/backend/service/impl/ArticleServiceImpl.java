package com.nexblog.backend.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.nexblog.backend.dto.article.ArticleCreateRequest;
import com.nexblog.backend.dto.article.ArticleDetailResponse;
import com.nexblog.backend.dto.article.ArticleListItemResponse;
import com.nexblog.backend.dto.article.ArticleQueryRequest;
import com.nexblog.backend.dto.article.ArticleUpdateRequest;
import com.nexblog.backend.entity.Article;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.ArticleRepository;
import com.nexblog.backend.service.ArticleService;

@Service
public class ArticleServiceImpl implements ArticleService {
    /*
     * 你现在第一优先功能：先打通“前台可读”能力
     * Step-1: listPublicArticles（首页列表）
     * Step-2: getPublicArticleDetail（详情页）
     * Step-3: 前端能展示后，再写 create/update/delete
     */

    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleListItemResponse> listPublicArticles(ArticleQueryRequest request) {
        /*
         * 设计目的：
         * 1) 把“分页参数默认值”“排序规则”“关键字过滤”都集中在 service，避免 controller 膨胀。
         * 2) 返回 Page<DTO>，让前端天然拿到 totalPages/totalElements 等分页元信息。
         * 3) 列表场景只返回轻量字段，不返回全文 content，减少网络传输体积。
         */
        int page = request.page() == null || request.page() < 0 ? 0 : request.page();
        int size = request.size() == null || request.size() <= 0 ? 10 : Math.min(request.size(), 50);

        /*
         * 参数含义：
         * - page: 第几页，从 0 开始（0 表示第一页）
         * - size: 每页条数，这里限制最大 50，避免一次查太多导致慢查询
         * - Sort.by("createTime").descending(): 按创建时间倒序，最新文章优先展示
         */
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());

        /*
         * 关键字策略：
         * - keyword 为空：查全部
         * - keyword 非空：按标题模糊查询（忽略大小写）
         */
        Page<Article> articlePage;
        if (!StringUtils.hasText(request.keyword())) {
            articlePage = articleRepository.findAll(pageable);
        } else {
            articlePage = articleRepository.findByTitleContainingIgnoreCase(request.keyword().trim(), pageable);
        }

        /*
         * entity -> list DTO 映射：
         * - id: 前端跳转详情页要用
         * - title: 列表标题
         * - summary: 卡片摘要
         * - createTime: 列表展示发布时间
         */
        return articlePage.map(this::toListItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getPublicArticleDetail(Long id) {
        /*
         * 设计目的：
         * 1) 把“文章不存在”的异常语义统一收敛到 service。
         * 2) controller 不需要关心仓储细节，只拿 DTO 直接返回。
         */
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException(10001, "文章不存在，id=" + id));
        return toDetailResponse(article);
    }

    @Override
    @Transactional
    public ArticleDetailResponse createArticle(ArticleCreateRequest request) {
        /*
         * 设计目的：
         * 1) create 只接收 DTO，不直接接 Entity，降低外部输入对数据库模型的耦合。
         * 2) 时间字段由后端统一赋值，避免前端伪造时间。
         */
        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setTitle(request.title().trim());
        article.setContent(request.content());
        article.setSummary(request.summary());
        article.setCreateTime(now);
        article.setUpDateTime(now);

        Article saved = articleRepository.save(article);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public ArticleDetailResponse updateArticle(Long id, ArticleUpdateRequest request) {
        /*
         * 设计目的：
         * 1) 更新前先查库并校验存在性。
         * 2) 仅修改允许变更的字段，保持写入行为可控。
         * 3) 每次更新刷新 upDateTime，便于后续做“最近更新”排序。
         */
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException(10001, "文章不存在，id=" + id));

        article.setTitle(request.title().trim());
        article.setContent(request.content());
        article.setSummary(request.summary());
        article.setUpDateTime(LocalDateTime.now());

        Article updated = articleRepository.save(article);
        return toDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        /*
         * 设计目的：
         * 1) 删除前做存在性校验，给前端更友好的错误信息。
         * 2) 不返回复杂结构，删除成功即返回 success + null data。
         */
        if (!articleRepository.existsById(id)) {
            throw new BusinessException(10001, "文章不存在，id=" + id);
        }
        articleRepository.deleteById(id);
    }

    private ArticleListItemResponse toListItemResponse(Article article) {
        return new ArticleListItemResponse(
            article.getId(),
            article.getTitle(),
            article.getSummary(),
            article.getCreateTime()
        );
    }

    private ArticleDetailResponse toDetailResponse(Article article) {
        return new ArticleDetailResponse(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getSummary(),
            article.getCreateTime(),
            article.getUpDateTime()
        );
    }
}
