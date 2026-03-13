package com.nexblog.backend.dto.config;

import jakarta.validation.constraints.NotBlank;

public record ConfigCreateRequest(
        @NotBlank
        String githubOwner,

        @NotBlank
        String githubRepo,

        @NotBlank
        String publishBranch,
        /*
         * 这里故意不加 @NotBlank：
         * - 首次保存配置时，前端会传 token
         * - 后续只改仓库名/分支时，前端可以不传 token，后端保留旧 token
         */
        String githubToken,

        @NotBlank
        String siteBasePath
) {
}
