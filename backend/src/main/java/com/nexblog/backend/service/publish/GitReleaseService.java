package com.nexblog.backend.service.publish;

import com.nexblog.backend.entity.SystemConfig;
import com.nexblog.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GitReleaseService {

    private static final DateTimeFormatter COMMIT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReleaseResult release(PublishWorkspaceService.PublishWorkspace workspace, SystemConfig config) {
        Path distRoot = Path.of(workspace.distRoot());
        if (!Files.exists(distRoot)) {
            throw new BusinessException(50031, "发布失败，dist 目录不存在: " + distRoot);
        }

        Path releaseRepoRoot = Path.of(workspace.jobRoot()).resolve("release-repo");
        try {
            recreateDirectory(releaseRepoRoot);
            copyDirectory(distRoot, releaseRepoRoot);
        } catch (IOException e) {
            throw new BusinessException(50032, "准备发布目录失败: " + e.getMessage());
        }

        String owner = config.getGithubOwner();
        String repo = config.getGithubRepo();
        String branch = config.getPublishBranch();
        String token = config.getGithubToken();
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String authenticatedRemote = "https://x-access-token:" + encodedToken + "@github.com/" + owner + "/" + repo + ".git";

        runCommand(List.of("git", "init", "-b", branch), releaseRepoRoot, token, encodedToken);
        runCommand(List.of("git", "config", "user.name", "NexBlog Bot"), releaseRepoRoot, token, encodedToken);
        runCommand(List.of("git", "config", "user.email", "noreply@nexblog.local"), releaseRepoRoot, token, encodedToken);
        runCommand(List.of("git", "add", "."), releaseRepoRoot, token, encodedToken);
        runCommand(
            List.of("git", "commit", "--allow-empty", "-m", "publish: " + COMMIT_TIME_FORMATTER.format(LocalDateTime.now())),
            releaseRepoRoot,
            token,
            encodedToken
        );
        runCommand(List.of("git", "remote", "add", "origin", authenticatedRemote), releaseRepoRoot, token, encodedToken);
        runCommand(List.of("git", "push", "--force", "origin", "HEAD:" + branch), releaseRepoRoot, token, encodedToken);

        String commitId = runCommand(List.of("git", "rev-parse", "HEAD"), releaseRepoRoot, token, encodedToken).trim();
        return new ReleaseResult(commitId, branch);
    }

    private String runCommand(List<String> command, Path cwd, String token, String encodedToken) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(cwd.toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().reduce("", (a, b) -> a + (a.isEmpty() ? "" : System.lineSeparator()) + b);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String safeOutput = maskSecret(output, token, encodedToken);
                String safeCommand = maskSecret(String.join(" ", command), token, encodedToken);
                throw new BusinessException(50033, "执行发布命令失败: " + safeCommand + System.lineSeparator() + safeOutput);
            }
            return output;
        } catch (IOException e) {
            throw new BusinessException(50034, "执行发布命令异常: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(50035, "执行发布命令被中断");
        }
    }

    private String maskSecret(String content, String token, String encodedToken) {
        String masked = content;
        if (token != null && !token.isBlank()) {
            masked = masked.replace(token, "***");
        }
        if (encodedToken != null && !encodedToken.isBlank()) {
            masked = masked.replace(encodedToken, "***");
        }
        return masked;
    }

    private void recreateDirectory(Path target) throws IOException {
        if (Files.exists(target)) {
            deleteDirectory(target);
        }
        Files.createDirectories(target);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new BusinessException(50036, "复制目录失败，源目录不存在: " + source);
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
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    private void deleteDirectory(Path target) throws IOException {
        try (var stream = Files.walk(target)) {
            List<Path> paths = new ArrayList<>(stream.toList());
            paths.sort(Comparator.reverseOrder());
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    public record ReleaseResult(
        String commitId,
        String branch
    ) {
    }
}
