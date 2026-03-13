package com.nexblog.backend.dto.publish;

public record PublishPrepareResponse(
    String jobId,
    int articleCount,
    String distPath,
    String previewPath
) {
}
