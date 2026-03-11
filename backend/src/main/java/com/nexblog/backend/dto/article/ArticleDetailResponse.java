package com.nexblog.backend.dto.article;

import java.time.LocalDateTime;

public record ArticleDetailResponse(
    Long id,
    String title,
    String content,
    String summary,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /*
     * 详情接口用于文章页展示，可逐步补充：
     * - tags / category
     * - viewCount
     * - publishedAt
     */
}
