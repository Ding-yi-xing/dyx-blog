package com.dyx.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * dyx-blog 后端启动类。
 * 负责启动 Spring Boot 应用并扫描 MyBatis Mapper 接口。
 */
@SpringBootApplication
@MapperScan("com.dyx.blog.mapper")
public class BlogApplication {

    /**
     * 后端服务启动入口。
     *
     * @param args 启动参数。
     */
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

    /**
     * 启动时补齐历史数据库缺失字段，避免旧库结构导致运行时报错。
     *
     * @param jdbcTemplate JDBC 模板。
     * @return 启动初始化任务。
     */
    @Bean
    public CommandLineRunner dyxSchemaInitializer(JdbcTemplate jdbcTemplate) {
        return args -> {
            ensureProfileTableColumn(jdbcTemplate);
            ensureWorkTable(jdbcTemplate);
            ensureSiteVisitStatTable(jdbcTemplate);
            ensureSiteVisitLogTable(jdbcTemplate);
        };
    }

    private void ensureProfileTableColumn(JdbcTemplate jdbcTemplate) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        ensureProfileColumn(jdbcTemplate, "resume_pdf_url", "ALTER TABLE dyx_profile ADD COLUMN resume_pdf_url VARCHAR(255)");
        ensureProfileColumn(jdbcTemplate, "hero_config", "ALTER TABLE dyx_profile ADD COLUMN hero_config LONGTEXT AFTER hero_subtitle");
    }

    private void ensureProfileColumn(JdbcTemplate jdbcTemplate, String columnName, String alterSql) {
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile' AND COLUMN_NAME = ?",
                Integer.class,
                columnName
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void ensureWorkTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_work ("
                + "id BIGINT PRIMARY KEY, "
                + "title VARCHAR(200) NOT NULL, "
                + "summary TEXT, "
                + "cover_image VARCHAR(255), "
                + "image_urls TEXT, "
                + "video_url VARCHAR(255), "
                + "video_poster VARCHAR(255), "
                + "work_link VARCHAR(255), "
                + "sort_order INT NOT NULL DEFAULT 0, "
                + "published TINYINT NOT NULL DEFAULT 0, "
                + "created_at DATETIME NOT NULL, "
                + "updated_at DATETIME NOT NULL)");
    }

    private void ensureSiteVisitStatTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_site_visit_stat ("
                + "page_key VARCHAR(64) PRIMARY KEY, "
                + "visit_count BIGINT NOT NULL DEFAULT 0, "
                + "updated_at DATETIME NOT NULL)");
    }

    private void ensureSiteVisitLogTable(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_site_visit_log ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                + "page_key VARCHAR(64) NOT NULL, "
                + "ip_address VARCHAR(45) NOT NULL, "
                + "user_agent VARCHAR(512), "
                + "device_type VARCHAR(32) NOT NULL, "
                + "device_name VARCHAR(128), "
                + "created_at DATETIME NOT NULL, "
                + "INDEX idx_site_visit_log_created_at (created_at), "
                + "INDEX idx_site_visit_log_page_key (page_key), "
                + "INDEX idx_site_visit_log_device_type (device_type))");
        ensureSiteVisitLogDeviceNameColumn(jdbcTemplate);
    }

    private void ensureSiteVisitLogDeviceNameColumn(JdbcTemplate jdbcTemplate) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_site_visit_log'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_site_visit_log' AND COLUMN_NAME = 'device_name'",
                Integer.class
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE dyx_site_visit_log ADD COLUMN device_name VARCHAR(128) AFTER device_type");
        }
    }
}
