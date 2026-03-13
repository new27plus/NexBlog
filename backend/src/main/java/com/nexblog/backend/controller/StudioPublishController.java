package com.nexblog.backend.controller;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.publish.PublishPrepareResponse;
import com.nexblog.backend.dto.publish.PublishReleaseRequest;
import com.nexblog.backend.dto.publish.PublishReleaseResponse;
import com.nexblog.backend.service.publish.PublishService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studio/publish")
public class StudioPublishController {

    private final PublishService publishService;

    public StudioPublishController(PublishService publishService) {
        this.publishService = publishService;
    }

    @PostMapping("/prepare")
    public ApiResponse<PublishPrepareResponse> prepare() {
        return ApiResponse.success(publishService.prepare());
    }

    @PostMapping("/release")
    public ApiResponse<PublishReleaseResponse> release(@Valid @RequestBody PublishReleaseRequest request) {
        return ApiResponse.success(publishService.release(request.jobId()));
    }
}
