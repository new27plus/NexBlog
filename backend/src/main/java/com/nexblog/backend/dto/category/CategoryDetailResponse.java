package com.nexblog.backend.dto.category;

import java.time.LocalDateTime;

public record CategoryDetailResponse(
    Long id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
