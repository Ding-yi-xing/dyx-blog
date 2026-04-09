package com.dyx.blog.task;

import com.dyx.blog.service.support.SiteVisitStatRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 定时将 Redis 中聚合的访问统计刷入 MySQL。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SiteVisitStatFlushScheduler {

    private final SiteVisitStatRedisService siteVisitStatRedisService;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelayString = "${dyx.site.visit-stat.flush-interval-ms:60000}")
    public void flushPendingVisitStats() {
        siteVisitStatRedisService.rotatePendingStats();
        Set<String> pendingFlushKeys = siteVisitStatRedisService.getPendingFlushKeys();
        for (String flushKey : pendingFlushKeys) {
            flushStats(siteVisitStatRedisService.loadFlushEntries(flushKey), flushKey);
        }
    }

    private void flushStats(Map<String, Long> stats, String flushKey) {
        if (stats == null || stats.isEmpty()) {
            if (flushKey != null) {
                siteVisitStatRedisService.clearFlushKey(flushKey);
            }
            return;
        }
        try {
            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                long increment = entry.getValue() == null ? 0L : entry.getValue();
                if (increment <= 0L) {
                    continue;
                }
                jdbcTemplate.update(
                        "INSERT INTO dyx_site_visit_stat (page_key, visit_count, updated_at) VALUES (?, ?, NOW()) "
                                + "ON DUPLICATE KEY UPDATE visit_count = visit_count + VALUES(visit_count), updated_at = NOW()",
                        entry.getKey(),
                        increment
                );
            }
            if (flushKey != null) {
                siteVisitStatRedisService.clearFlushKey(flushKey);
            }
        } catch (Exception exception) {
            log.error("刷新访问统计失败 [{}]: {}", flushKey == null ? "pending" : flushKey, exception.getMessage(), exception);
        }
    }
}
