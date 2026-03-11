package com.nexblog.backend.dto.article;

public record ArticleQueryRequest(
    Integer page,
    Integer size,
    String keyword
) {
    /*
     * 建议你在 service 里做默认值兜底：
     * - page 默认 0
     * - size 默认 10 或 20
     * - keyword 为空时查询全部
     */
}
