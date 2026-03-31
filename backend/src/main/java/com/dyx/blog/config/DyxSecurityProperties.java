package com.dyx.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DYX 安全相关配置属性。
 * 用于集中管理加密密钥与跨域来源配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "dyx.security")
public class DyxSecurityProperties {

    /**
     * AES 加密密钥。
     */
    private String encryptKey = "dyx-blog-default-key-32chars!!!";

    /**
     * 允许跨域访问的来源模式。
     */
    private List<String> corsAllowedOrigins = new ArrayList<>();
}
