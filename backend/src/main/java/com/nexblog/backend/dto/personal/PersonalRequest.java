package com.nexblog.backend.dto.personal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record PersonalRequest(
        @NotNull Map<String, List<@Valid PersonalItem>> sections
) {
}
