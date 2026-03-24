package com.dyx.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置属性。
 * 用于指定上传目录和对外访问前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /** 文件上传目录。 */
    private String uploadPath;

    /** 文件访问前缀。 */
    private String accessPrefix;
}
