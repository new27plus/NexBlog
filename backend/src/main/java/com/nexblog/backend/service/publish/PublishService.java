package com.nexblog.backend.service.publish;

import com.nexblog.backend.dto.publish.PublishPrepareResponse;
import com.nexblog.backend.dto.publish.PublishReleaseResponse;
import com.nexblog.backend.entity.SystemConfig;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.ConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PublishService {

    private final PublishWorkspaceService publishWorkspaceService;
    private final StaticExportService staticExportService;
    private final FrontendBuildService frontendBuildService;
    private final GitReleaseService gitReleaseService;
    private final ConfigRepository configRepository;

    public PublishService(
        PublishWorkspaceService publishWorkspaceService,
        StaticExportService staticExportService,
        FrontendBuildService frontendBuildService,
        GitReleaseService gitReleaseService,
        ConfigRepository configRepository
    ) {
        this.publishWorkspaceService = publishWorkspaceService;
        this.staticExportService = staticExportService;
        this.frontendBuildService = frontendBuildService;
        this.gitReleaseService = gitReleaseService;
        this.configRepository = configRepository;
    }

    public PublishPrepareResponse prepare() {
        PublishWorkspaceService.PublishWorkspace workspace = publishWorkspaceService.prepareWorkspace();
        StaticExportService.ExportResult exportResult = staticExportService.exportArticles(workspace);
        FrontendBuildService.BuildResult buildResult = frontendBuildService.build(
            workspace,
            buildPreviewBasePath(workspace.jobId())
        );

        return new PublishPrepareResponse(
            workspace.jobId(),
            exportResult.articleCount(),
            buildResult.distPath(),
            buildPreviewBasePath(workspace.jobId())
        );
    }

    public PublishReleaseResponse release(String jobId) {
        SystemConfig config = requireConfig(true);
        PublishWorkspaceService.PublishWorkspace workspace = publishWorkspaceService.resolveWorkspace(jobId);
        frontendBuildService.build(workspace, config.getSiteBasePath());
        GitReleaseService.ReleaseResult releaseResult = gitReleaseService.release(workspace, config);

        return new PublishReleaseResponse(
            workspace.jobId(),
            releaseResult.branch(),
            releaseResult.commitId(),
            buildPublishedUrl(config)
        );
    }

    public Path resolvePreviewFile(String jobId, String relativePath) {
        PublishWorkspaceService.PublishWorkspace workspace = publishWorkspaceService.resolveWorkspace(jobId);
        Path distRoot = Path.of(workspace.distRoot()).toAbsolutePath().normalize();
        String candidate = sanitizeRelativePath(relativePath);
        Path resolved = distRoot.resolve(candidate).normalize();
        if (!resolved.startsWith(distRoot) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
            throw new BusinessException(50041, "预览文件不存在");
        }
        return resolved;
    }

    private String sanitizeRelativePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return "index.html";
        }
        String candidate = relativePath.trim().replace('\\', '/');
        while (candidate.startsWith("/")) {
            candidate = candidate.substring(1);
        }
        return StringUtils.hasText(candidate) ? candidate : "index.html";
    }

    private String buildPreviewBasePath(String jobId) {
        return "/preview/" + jobId + "/";
    }

    private SystemConfig requireConfig(boolean requireToken) {
        SystemConfig config = configRepository.findFirstByOrderByIdAsc()
            .orElseThrow(() -> new BusinessException(50042, "请先保存 GitHub 配置"));

        if (!StringUtils.hasText(config.getGithubOwner())
            || !StringUtils.hasText(config.getGithubRepo())
            || !StringUtils.hasText(config.getPublishBranch())
            || !StringUtils.hasText(config.getSiteBasePath())) {
            throw new BusinessException(50043, "GitHub 配置不完整，请先在全局设置页保存");
        }

        if (requireToken && !StringUtils.hasText(config.getGithubToken())) {
            throw new BusinessException(50044, "未配置 GitHub Token，无法发布到 GitHub");
        }

        return config;
    }

    private String buildPublishedUrl(SystemConfig config) {
        String owner = config.getGithubOwner().trim();
        String repo = config.getGithubRepo().trim();
        String siteBasePath = config.getSiteBasePath().trim();
        String normalizedBasePath = siteBasePath.startsWith("/") ? siteBasePath : "/" + siteBasePath;
        if (!normalizedBasePath.endsWith("/")) {
            normalizedBasePath = normalizedBasePath + "/";
        }
        if ("/".equals(normalizedBasePath) && repo.equalsIgnoreCase(owner + ".github.io")) {
            return "https://" + owner + ".github.io/";
        }
        if ("/".equals(normalizedBasePath)) {
            return "https://" + owner + ".github.io/" + repo + "/";
        }
        return "https://" + owner + ".github.io" + normalizedBasePath;
    }
}
