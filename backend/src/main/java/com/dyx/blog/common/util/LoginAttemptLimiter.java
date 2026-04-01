package com.dyx.blog.common.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限速器。
 * 按用户名与 IP 双维度统计短时间内的失败次数，降低暴力尝试风险。
 */
@Component
public class LoginAttemptLimiter {

    /** 单个时间窗口内允许的最大失败次数。 */
    private static final int MAX_ATTEMPTS = 5;

    /** 失败次数统计窗口，超时后自动重置。 */
    private static final Duration WINDOW = Duration.ofMinutes(10);

    /** 按用户名或 IP 维度缓存登录失败窗口。 */
    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    /**
     * 校验当前用户名与 IP 是否仍允许继续登录尝试。
     * 任一维度超过阈值都会抛出异常并阻止后续认证。
     */
    public void check(String username, String ipAddress) {
        ensureAllowed(buildKey("ip", ipAddress));
        ensureAllowed(buildKey("user", username));
    }

    /**
     * 记录一次登录失败，同时累加用户名与 IP 维度的失败计数。
     */
    public void recordFailure(String username, String ipAddress) {
        increment(buildKey("ip", ipAddress));
        increment(buildKey("user", username));
    }

    /**
     * 登录成功后清除对应用户名与 IP 的失败窗口。
     */
    public void recordSuccess(String username, String ipAddress) {
        attempts.remove(buildKey("ip", ipAddress));
        attempts.remove(buildKey("user", username));
    }

    /**
     * 检查指定统计键是否仍在有效窗口内且未超过阈值。
     */
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

    /**
     * 递增失败次数；若窗口已过期则重新开始计数。
     */
    private void increment(String key) {
        attempts.compute(key, (ignored, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptWindow(1, Instant.now().plus(WINDOW));
            }
            existing.count += 1;
            return existing;
        });
    }

    /**
     * 构建统一的统计键，空值会折叠为 unknown。
     */
    private String buildKey(String prefix, String value) {
        String normalized = value == null || value.trim().isEmpty() ? "unknown" : value.trim().toLowerCase();
        return prefix + ':' + normalized;
    }

    /**
     * 单个统计窗口对象，记录当前失败次数与过期时间。
     */
    private static final class AttemptWindow {
        private int count;
        private final Instant expiresAt;

        private AttemptWindow(int count, Instant expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }

        /**
         * 判断当前窗口是否已过期。
         */
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
