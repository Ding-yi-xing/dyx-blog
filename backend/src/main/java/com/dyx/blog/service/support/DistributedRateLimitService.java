package com.dyx.blog.service.support;

import com.dyx.blog.config.SiteProtectionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 分布式限流服务。
 */
@Service
@RequiredArgsConstructor
public class DistributedRateLimitService {

    private static final DefaultRedisScript<Long> FIXED_WINDOW_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) "
                    + "if current == 1 then redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2])) end "
                    + "if current > tonumber(ARGV[1]) then return 0 end "
                    + "return 1",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final SiteProtectionProperties siteProtectionProperties;

    public boolean allowGuestbook(String clientIp) {
        SiteProtectionProperties.LimitRuleProperties rule = siteProtectionProperties.getRateLimit().getGuestbook();
        return allow(buildGuestbookKey(clientIp), rule);
    }

    public boolean allowVisit(String clientIp, String pageKey) {
        SiteProtectionProperties.LimitRuleProperties rule = siteProtectionProperties.getRateLimit().getVisit();
        return allow(buildVisitKey(clientIp, pageKey), rule);
    }

    private boolean allow(String key, SiteProtectionProperties.LimitRuleProperties rule) {
        if (rule == null || !rule.isEnabled()) {
            return true;
        }
        Long result = stringRedisTemplate.execute(
                FIXED_WINDOW_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(Math.max(rule.getLimit(), 1L)),
                String.valueOf(Math.max(rule.getWindowSeconds(), 1L))
        );
        return Long.valueOf(1L).equals(result);
    }

    private String buildGuestbookKey(String clientIp) {
        return "dyx:rate_limit:guestbook:" + normalizeSegment(clientIp);
    }

    private String buildVisitKey(String clientIp, String pageKey) {
        return "dyx:rate_limit:visit:" + normalizeSegment(clientIp) + ':' + normalizeSegment(pageKey);
    }

    private String normalizeSegment(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        return value.trim().toLowerCase();
    }
}
