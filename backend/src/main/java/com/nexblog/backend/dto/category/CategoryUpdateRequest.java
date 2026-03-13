package com.nexblog.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
    @NotBlank(message = "分类名不能为空")
    @Size(max = 100, message = "分类名最长100字")
    String name
) {

}
