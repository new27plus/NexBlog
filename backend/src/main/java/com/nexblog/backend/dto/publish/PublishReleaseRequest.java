package com.nexblog.backend.dto.publish;

import jakarta.validation.constraints.NotBlank;

public record PublishReleaseRequest(
    @NotBlank
    String jobId
) {
}
