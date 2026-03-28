package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体。
 */
@Data
@TableName("dyx_system_config")
public class SystemConfig {

    @TableId
    private Long id;
    private String storageType;
    private String ossEndpoint;
    private String ossRegion;
    private String ossBucketName;
    private String ossPublicUrlPrefix;
    private String ossBaseDir;
    private String footprintEyebrow;
    private String footprintTitle;
    private String footprintSubtitle;
    private String footprintDescription;
    private String copyrightText;
    private String techSupportText;
    private LocalDateTime updatedAt;
}
