package com.nexblog.backend.service.publish;

import com.nexblog.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FrontendBuildService {

    private final String frontendRoot;

    public FrontendBuildService(@Value("${nexblog.publish.frontend-root:../front}") String frontendRoot) {
        this.frontendRoot = frontendRoot;
    }

    public BuildResult build(PublishWorkspaceService.PublishWorkspace workspace, String siteBasePath) {
        Path frontRootPath = Path.of(frontendRoot).toAbsolutePath().normalize();
        if (!Files.exists(frontRootPath.resolve("package.json"))) {
            throw new BusinessException(50021, "前端目录无效，未找到 package.json: " + frontRootPath);
        }

        Path workspaceExportRoot = Path.of(workspace.exportRoot());
        Path frontExportRoot = frontRootPath.resolve("public").resolve("export");
        Path frontDistRoot = frontRootPath.resolve("dist");
        Path workspaceDistRoot = Path.of(workspace.distRoot());

        try {
            recreateDirectory(frontExportRoot);
            copyDirectory(workspaceExportRoot, frontExportRoot);
            runBuildCommand(frontRootPath, normalizeBasePath(siteBasePath));
            recreateDirectory(workspaceDistRoot);
            copyDirectory(frontDistRoot, workspaceDistRoot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(50022, "前端构建失败: " + e.getMessage());
        } catch (IOException | UncheckedIOException e) {
            throw new BusinessException(50022, "前端构建失败: " + e.getMessage());
        }

        return new BuildResult(workspace.jobId(), workspaceDistRoot.toString());
    }

    private void runBuildCommand(Path frontRootPath, String basePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(List.of(resolveNpmCommand(), "run", "build-only"));
        processBuilder.directory(frontRootPath.toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("VITE_STATIC_EXPORT", "true");
        processBuilder.environment().put("VITE_SITE_BASE_PATH", basePath);

        Process process = processBuilder.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new BusinessException(50023, "前端构建命令执行失败: " + output);
        }
    }

    private String resolveNpmCommand() {
        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().contains("win")) {
            return "npm.cmd";
        }
        return "npm";
    }

    private String normalizeBasePath(String siteBasePath) {
        String normalized = StringUtils.hasText(siteBasePath) ? siteBasePath.trim() : "/";
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private void recreateDirectory(Path target) throws IOException {
        if (Files.exists(target)) {
            deleteDirectory(target);
        }
        Files.createDirectories(target);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new BusinessException(50024, "复制目录失败，源目录不存在: " + source);
        }
        try (var stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path resolved = target.resolve(relative);
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(resolved);
                    } else {
                        Files.copy(path, resolved, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void deleteDirectory(Path target) throws IOException {
        try (var stream = Files.walk(target)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public record BuildResult(
        String jobId,
        String distPath
    ) {
    }
}
