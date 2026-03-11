package com.nexblog.backend.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleCreateRequest(
    @NotBlank(message = "title 不能为空")
    @Size(max = 100, message = "title 最长 100 字")
    String title,

    @NotBlank(message = "content 不能为空")
    String content,

    @Size(max = 300, message = "summary 最长 300 字")
    String summary
) {
    /*
     * 你后续可按需继续加字段：
     * 1) status (DRAFT/PUBLISHED)
     * 2) coverUrl
     * 3) tagIds / categoryId
     */
}
