package com.dyx.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 站点防护与访问统计相关配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dyx.site")
public class SiteProtectionProperties {

    private VisitStatProperties visitStat = new VisitStatProperties();
    private RateLimitProperties rateLimit = new RateLimitProperties();

    @Data
    public static class VisitStatProperties {
        /**
         * Redis 待刷新的访问统计 Hash key。
         */
        private String pendingKey = "dyx:site:visit:stat:pending";

        /**
         * Redis 刷库中的访问统计 Hash key 前缀。
         */
        private String flushKeyPrefix = "dyx:site:visit:stat:flush:";

        /**
         * Redis 记录待处理 flush key 的集合。
         */
        private String flushIndexKey = "dyx:site:visit:stat:flush:index";

        /**
         * 批量刷库时间间隔（毫秒）。
         */
        private long flushIntervalMs = 60000L;
    }

    @Data
    public static class RateLimitProperties {
        private LimitRuleProperties guestbook = new LimitRuleProperties(true, 5, 300);
        private LimitRuleProperties visit = new LimitRuleProperties(true, 60, 60);
    }

    @Data
    public static class LimitRuleProperties {
        private boolean enabled;
        private long limit;
        private long windowSeconds;

        public LimitRuleProperties() {
        }

        public LimitRuleProperties(boolean enabled, long limit, long windowSeconds) {
            this.enabled = enabled;
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }
}
