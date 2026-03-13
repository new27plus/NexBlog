package com.nexblog.backend.controller;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.config.ConfigCreateRequest;
import com.nexblog.backend.dto.config.ConfigDetailResponse;
import com.nexblog.backend.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studio/system-config")
public class SystemConfigController {

    private final ConfigService configService;

    public SystemConfigController(ConfigService configService) {
        this.configService = configService;
    }


    @GetMapping
    public ApiResponse<ConfigDetailResponse> getConfig() {
        return ApiResponse.success(configService.getConfig());
    }


    @PutMapping
    public ApiResponse<ConfigDetailResponse> setConfig(@Valid @RequestBody ConfigCreateRequest request) {
        return ApiResponse.success(configService.upsertConfig(request));
    }

}
