package com.nexblog.backend.controller;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.config.AuthTokenInterceptor;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/bootstrap-status")
    public ApiResponse<BootstrapStatusResponse> bootstrapStatus() {
        return ApiResponse.success(new BootstrapStatusResponse(authService.isBootstrapRequired()));
    }

    @PostMapping("/bootstrap")
    public ApiResponse<LoginResponse> bootstrap(@Valid @RequestBody BootstrapRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(30007, "两次输入的密码不一致");
        }
        AuthService.LoginResult result = authService.bootstrap(request.username().trim(), request.password());
        return ApiResponse.success(new LoginResponse(result.username(), result.accessToken(), result.expiresAt()));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (authService.isBootstrapRequired()) {
            throw new BusinessException(30005, "系统尚未初始化，请先创建管理员账号");
        }
        AuthService.LoginResult result = authService.login(request.username().trim(), request.password());
        return ApiResponse.success(new LoginResponse(result.username(), result.accessToken(), result.expiresAt()));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> me(HttpServletRequest request) {
        String username = (String) request.getAttribute(AuthTokenInterceptor.REQUEST_USER_KEY);
        return ApiResponse.success(new UserInfoResponse(username));
    }

    public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password
    ) { }

    public record BootstrapRequest(
        @NotBlank(message = "用户名不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password,
        @NotBlank(message = "确认密码不能为空")
        String confirmPassword
    ) { }

    public record LoginResponse(
        String username,
        String accessToken,
        String expiresAt
    ) { }

    public record BootstrapStatusResponse(
        boolean bootstrapRequired
    ) { }

    public record UserInfoResponse(
        String username
    ) { }
}
