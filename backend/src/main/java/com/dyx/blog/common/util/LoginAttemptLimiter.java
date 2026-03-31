package com.dyx.blog.common.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限速器。
 */
@Component
public class LoginAttemptLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void check(String username, String ipAddress) {
        ensureAllowed(buildKey("ip", ipAddress));
        ensureAllowed(buildKey("user", username));
    }

    public void recordFailure(String username, String ipAddress) {
        increment(buildKey("ip", ipAddress));
        increment(buildKey("user", username));
    }

    public void recordSuccess(String username, String ipAddress) {
        attempts.remove(buildKey("ip", ipAddress));
        attempts.remove(buildKey("user", username));
    }

    private void ensureAllowed(String key) {
        AttemptWindow window = attempts.get(key);
        if (window == null) {
            return;
        }
        if (window.isExpired()) {
            attempts.remove(key);
            return;
        }
        if (window.count >= MAX_ATTEMPTS) {
            throw new IllegalStateException("登录尝试过于频繁，请稍后再试");
        }
    }

    private void increment(String key) {
        attempts.compute(key, (ignored, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptWindow(1, Instant.now().plus(WINDOW));
            }
            existing.count += 1;
            return existing;
        });
    }

    private String buildKey(String prefix, String value) {
        String normalized = value == null || value.trim().isEmpty() ? "unknown" : value.trim().toLowerCase();
        return prefix + ':' + normalized;
    }

    private static final class AttemptWindow {
        private int count;
        private final Instant expiresAt;

        private AttemptWindow(int count, Instant expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
