package com.nexblog.backend.dto.config;

public record ConfigDetailResponse(
        Long id,
        String githubOwner,
        String githubRepo,
        String publishBranch,
        boolean tokenConfigured,
        String siteBasePath
) {
}
