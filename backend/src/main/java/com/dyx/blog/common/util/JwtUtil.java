package com.dyx.blog.common.util;

import com.dyx.blog.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 工具类。
 * 用于生成令牌并解析当前登录用户 ID。
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties dyxJwtProperties;

    /**
     * 生成指定用户的 JWT 令牌。
     *
     * @param userId 用户主键。
     * @return JWT 字符串。
     */
    public String generateToken(Long userId) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(dyxJwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 从 JWT 中解析用户主键。
     *
     * @param token JWT 字符串。
     * @return 用户主键。
     */
    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 获取签名密钥。
     *
     * @return HMAC 签名密钥。
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(dyxJwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
