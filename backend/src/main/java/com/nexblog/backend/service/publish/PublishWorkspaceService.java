package com.nexblog.backend.service.publish;

import com.nexblog.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PublishWorkspaceService {

    private static final DateTimeFormatter JOB_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern JOB_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9-]+$");

    private final String workspaceRoot;

    public PublishWorkspaceService(@Value("${nexblog.publish.workspace-root:./work/publish}") String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public PublishWorkspace prepareWorkspace() {
        String jobId = JOB_TIME_FORMATTER.format(LocalDateTime.now()) + "-" + UUID.randomUUID().toString().substring(0, 8);

        Path rootPath = Path.of(workspaceRoot).toAbsolutePath().normalize();
        Path jobRoot = rootPath.resolve(jobId);
        Path exportRoot = jobRoot.resolve("export");
        Path exportArticlesRoot = exportRoot.resolve("articles");
        Path distRoot = jobRoot.resolve("dist");

        try {
            Files.createDirectories(exportArticlesRoot);
            Files.createDirectories(distRoot);
        } catch (IOException e) {
            throw new BusinessException(50001, "创建发布工作目录失败: " + e.getMessage());
        }

        return new PublishWorkspace(
            jobId,
            jobRoot.toString(),
            exportRoot.toString(),
            exportArticlesRoot.toString(),
            distRoot.toString()
        );
    }

    public PublishWorkspace resolveWorkspace(String jobId) {
        if (!JOB_ID_PATTERN.matcher(jobId).matches()) {
            throw new BusinessException(50002, "无效的发布任务 ID");
        }

        Path rootPath = Path.of(workspaceRoot).toAbsolutePath().normalize();
        Path jobRoot = rootPath.resolve(jobId).normalize();
        if (!jobRoot.startsWith(rootPath) || !Files.exists(jobRoot)) {
            throw new BusinessException(50003, "发布任务不存在");
        }

        Path exportRoot = jobRoot.resolve("export");
        Path exportArticlesRoot = exportRoot.resolve("articles");
        Path distRoot = jobRoot.resolve("dist");

        return new PublishWorkspace(
            jobId,
            jobRoot.toString(),
            exportRoot.toString(),
            exportArticlesRoot.toString(),
            distRoot.toString()
        );
    }

    public PublishWorkspace resolveLatestWorkspace() {
        Path rootPath = Path.of(workspaceRoot).toAbsolutePath().normalize();
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            return null;
        }

        try (var stream = Files.list(rootPath)) {
            Path latestJobRoot = stream
                .filter(Files::isDirectory)
                .filter(path -> Files.exists(path.resolve("dist").resolve("index.html")))
                .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                .findFirst()
                .orElse(null);
            if (latestJobRoot == null) {
                return null;
            }
            String jobId = latestJobRoot.getFileName().toString();
            return resolveWorkspace(jobId);
        } catch (IOException e) {
            throw new BusinessException(50004, "读取最近发布任务失败: " + e.getMessage());
        }
    }

    public record PublishWorkspace(
        String jobId,
        String jobRoot,
        String exportRoot,
        String exportArticlesRoot,
        String distRoot
    ) {
    }
}
