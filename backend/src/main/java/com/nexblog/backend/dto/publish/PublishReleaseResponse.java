package com.nexblog.backend.dto.publish;

public record PublishReleaseResponse(
    String jobId,
    String branch,
    String commitId,
    String publishedUrl
) {
}
