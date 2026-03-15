package com.nexblog.backend.dto.personal;

import jakarta.validation.constraints.NotBlank;

public record PersonalItem(
        @NotBlank String id,
        String title,
        String subtitle,
        String content,
        String tag,
        String linkIcon,
        String linkLabel,
        String linkUrl
) {
}
