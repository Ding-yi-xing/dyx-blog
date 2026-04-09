package com.dyx.blog.service.support;

import com.dyx.blog.config.SiteProtectionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 访问统计 Redis 聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteVisitStatRedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SiteProtectionProperties siteProtectionProperties;

    public void increment(String pageKey) {
        stringRedisTemplate.opsForHash().increment(getPendingKey(), pageKey, 1L);
    }

    public long getPendingTotal() {
        long total = sum(stringRedisTemplate.opsForHash().entries(getPendingKey()));
        for (String flushKey : getPendingFlushKeys()) {
            total += sum(stringRedisTemplate.opsForHash().entries(flushKey));
        }
        return total;
    }

    public void rotatePendingStats() {
        String pendingKey = getPendingKey();
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(pendingKey))) {
            return;
        }
        String flushKey = getFlushKeyPrefix() + Instant.now().toEpochMilli();
        try {
            Boolean renamed = stringRedisTemplate.renameIfAbsent(pendingKey, flushKey);
            if (!Boolean.TRUE.equals(renamed)) {
                return;
            }
        } catch (RedisSystemException exception) {
            log.debug("访问统计 pending key 不存在，跳过本次刷库轮转: {}", pendingKey);
            return;
        }
        stringRedisTemplate.opsForSet().add(getFlushIndexKey(), flushKey);
        stringRedisTemplate.expire(flushKey, Duration.ofDays(1));
    }

    public Set<String> getPendingFlushKeys() {
        Set<String> keys = stringRedisTemplate.opsForSet().members(getFlushIndexKey());
        return keys == null ? Collections.emptySet() : keys;
    }

    public Map<String, Long> loadFlushEntries(String flushKey) {
        Map<Object, Object> rawEntries = stringRedisTemplate.opsForHash().entries(flushKey);
        if (rawEntries == null || rawEntries.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Long> entries = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
            String pageKey = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            entries.put(pageKey, parseLong(value));
        }
        return entries;
    }

    public void clearFlushKey(String flushKey) {
        stringRedisTemplate.delete(flushKey);
        stringRedisTemplate.opsForSet().remove(getFlushIndexKey(), flushKey);
    }

    private long sum(Map<Object, Object> entries) {
        long total = 0L;
        for (Object value : entries.values()) {
            if (value == null) {
                continue;
            }
            total += parseLong(value);
        }
        return total;
    }

    private long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String getPendingKey() {
        return siteProtectionProperties.getVisitStat().getPendingKey();
    }

    private String getFlushKeyPrefix() {
        return siteProtectionProperties.getVisitStat().getFlushKeyPrefix();
    }

    private String getFlushIndexKey() {
        return siteProtectionProperties.getVisitStat().getFlushIndexKey();
    }
}
