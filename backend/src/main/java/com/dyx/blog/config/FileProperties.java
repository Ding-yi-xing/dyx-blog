package com.dyx.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性。
 * 用于指定本地上传目录、访问前缀与上传大小限制。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /** 本地或 OSS 存储模式。 */
    private String storageType = "local";

    /** 本地文件上传目录。 */
    private String uploadPath = "uploads/";

    /** 本地文件访问前缀。 */
    private String accessPrefix;

    /**
     * 单个上传文件的最大大小（字节），用于业务层校验。
     * 默认 100MB，可通过配置覆盖。
     */
    private long maxUploadSizeBytes = 100L * 1024 * 1024;
}
