package com.nexblog.backend.service.auth;

import com.nexblog.backend.config.AuthProperties;
import com.nexblog.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final AuthProperties authProperties;

    public TokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public TokenPayload createToken(String username) {
        long expiresAtEpochSecond = Instant.now().plusSeconds(authProperties.tokenTtlMinutes() * 60).getEpochSecond();
        String payload = username + "|" + expiresAtEpochSecond + "|" + UUID.randomUUID();
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String signature = sign(encodedPayload);
        String token = encodedPayload + "." + signature;
        return new TokenPayload(token, expiresAtEpochSecond);
    }

    public String verifyAndGetUsername(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }

        String encodedPayload = parts[0];
        String signature = parts[1];
        String expectedSignature = sign(encodedPayload);
        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }

        String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|", 3);
        if (values.length < 2) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }

        String username = values[0];
        long expiresAtEpochSecond;
        try {
            expiresAtEpochSecond = Long.parseLong(values[1]);
        } catch (NumberFormatException ex) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }

        if (Instant.now().getEpochSecond() >= expiresAtEpochSecond) {
            throw new BusinessException(30001, "未登录或登录已过期");
        }

        return username;
    }

    private String sign(String encodedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(authProperties.tokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signed = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signed);
        } catch (Exception ex) {
            throw new BusinessException(30002, "鉴权服务初始化失败");
        }
    }

    public record TokenPayload(
        String token,
        long expiresAtEpochSecond
    ) { }
}
