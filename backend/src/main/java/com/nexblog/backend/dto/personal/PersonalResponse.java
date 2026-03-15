package com.nexblog.backend.dto.personal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record PersonalResponse(
        Long id,
        Map<String, List<PersonalItem>> sections,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
