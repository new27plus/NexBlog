package com.nexblog.backend.service.auth;

import com.nexblog.backend.entity.AdminAccount;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.AdminAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final TokenService tokenService;
    private final PasswordHasher passwordHasher;
    private final AdminAccountRepository adminAccountRepository;

    public AuthService(
        TokenService tokenService,
        PasswordHasher passwordHasher,
        AdminAccountRepository adminAccountRepository
    ) {
        this.tokenService = tokenService;
        this.passwordHasher = passwordHasher;
        this.adminAccountRepository = adminAccountRepository;
    }

    @Transactional(readOnly = true)
    public boolean isBootstrapRequired() {
        return adminAccountRepository.findFirstByOrderByIdAsc().isEmpty();
    }

    @Transactional
    public LoginResult bootstrap(String username, String password) {
        if (!isBootstrapRequired()) {
            throw new BusinessException(30006, "系统已初始化，不能重复创建管理员账号");
        }

        String normalizedUsername = username.trim();
        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hash(password, salt);

        AdminAccount account = new AdminAccount();
        account.setUsername(normalizedUsername);
        account.setPasswordSalt(salt);
        account.setPasswordHash(hash);
        account.setCreateTime(LocalDateTime.now());
        account.setUpDateTime(LocalDateTime.now());
        adminAccountRepository.save(account);

        return createLoginResult(normalizedUsername);
    }

    @Transactional(readOnly = true)
    public LoginResult login(String username, String password) {
        AdminAccount account = adminAccountRepository.findByUsername(username.trim())
            .orElseThrow(() -> new BusinessException(30003, "用户名或密码错误"));

        String hashedPassword = passwordHasher.hash(password, account.getPasswordSalt());
        boolean passwordMatched = MessageDigest.isEqual(
            hashedPassword.getBytes(StandardCharsets.UTF_8),
            account.getPasswordHash().getBytes(StandardCharsets.UTF_8)
        );
        if (!passwordMatched) {
            throw new BusinessException(30003, "用户名或密码错误");
        }

        return createLoginResult(account.getUsername());
    }

    @Transactional(readOnly = true)
    public SessionUser verifyToken(String token) {
        String username = tokenService.verifyAndGetUsername(token);
        AdminAccount account = adminAccountRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(30001, "未登录或登录已过期"));
        return new SessionUser(account.getUsername());
    }

    private LoginResult createLoginResult(String username) {
        TokenService.TokenPayload tokenPayload = tokenService.createToken(username);
        Instant expiresAt = Instant.ofEpochSecond(tokenPayload.expiresAtEpochSecond());
        return new LoginResult(username, tokenPayload.token(), expiresAt.toString());
    }

    public record LoginResult(
        String username,
        String accessToken,
        String expiresAt
    ) { }

    public record SessionUser(
        String username
    ) { }
}
