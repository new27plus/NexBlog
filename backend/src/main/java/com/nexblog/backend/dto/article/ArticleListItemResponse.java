package com.nexblog.backend.dto.article;

import java.time.LocalDateTime;

public record ArticleListItemResponse(
    Long id,
    String title,
    String summary,
    LocalDateTime createdAt
) {
    /*
     * 列表页响应只放“轻量字段”，不要直接把全文 content 返回。
     * 这样列表查询更快，前端渲染也更轻。
     */
}
