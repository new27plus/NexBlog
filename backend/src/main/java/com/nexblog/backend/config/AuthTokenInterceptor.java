package com.nexblog.backend.config;

import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthTokenInterceptor implements HandlerInterceptor {

    public static final String REQUEST_USER_KEY = "studioUsername";

    private final AuthService authService;

    public AuthTokenInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }
        AuthService.SessionUser user = authService.verifyToken(token);
        request.setAttribute(REQUEST_USER_KEY, user.username());
        return true;
    }
}
