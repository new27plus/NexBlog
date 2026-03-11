package com.nexblog.backend.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleUpdateRequest(
    @NotBlank(message = "title 不能为空")
    @Size(max = 100, message = "title 最长 100 字")
    String title,

    @NotBlank(message = "content 不能为空")
    String content,

    @Size(max = 300, message = "summary 最长 300 字")
    String summary
) {
    /*
     * 更新接口建议你支持“部分更新”或“全量更新”二选一：
     * - 全量更新：字段都必填（当前结构）
     * - 部分更新：字段可空，service 内逐个判断再更新
     */
}
