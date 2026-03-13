package com.nexblog.backend.dto.category;

public record CategoryQueryRequest(
    Integer page,
    Integer size,
    String keyword
) {

}
