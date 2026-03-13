package com.nexblog.backend.dto.category;

import java.time.LocalDateTime;

public record CategoryListItemResponse(
    Long id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
