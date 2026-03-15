package com.nexblog.backend.controller;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.dto.personal.PersonalRequest;
import com.nexblog.backend.dto.personal.PersonalResponse;
import com.nexblog.backend.service.PersonalService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonalConfigController {

    private final PersonalService personalService;

    public PersonalConfigController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping("/api/studio/personal-config")
    public ApiResponse<PersonalResponse> getStudioPersonalConfig() {
        return ApiResponse.success(personalService.getStudioPersonalConfig());
    }

    @PutMapping("/api/studio/personal-config")
    public ApiResponse<PersonalResponse> updateStudioPersonalConfig(@Valid @RequestBody PersonalRequest request) {
        return ApiResponse.success(personalService.updatePersonalConfig(request));
    }

    @GetMapping("/api/personal-config")
    public ApiResponse<PersonalResponse> getPersonalConfig() {
        return ApiResponse.success(personalService.getPersonalConfig());
    }
}
