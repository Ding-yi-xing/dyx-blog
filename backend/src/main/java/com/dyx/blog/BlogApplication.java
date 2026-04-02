package com.dyx.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.TimeZone;

/**
 * dyx-blog 后端启动类。
 * 负责启动 Spring Boot 应用并扫描 MyBatis Mapper 接口。
 */
@SpringBootApplication
@EnableCaching
@MapperScan("com.dyx.blog.mapper")
public class BlogApplication {

    private static final String APP_TIME_ZONE = "Asia/Shanghai";

    /**
     * 后端服务启动入口。
     *
     * @param args 启动参数。
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(APP_TIME_ZONE));
        SpringApplication.run(BlogApplication.class, args);
    }
}
