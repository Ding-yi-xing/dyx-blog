package com.dyx.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台系统配置返回对象。
 */
@Data
public class AdminSystemConfigVo {

    private Long id;
    private String storageType;
    private String ossBaseDir;
    private String footprintEyebrow;
    private String footprintTitle;
    private String footprintSubtitle;
    private String footprintDescription;
    private String copyrightText;
    private String techSupportText;
    private LocalDateTime updatedAt;

    private Boolean ossEndpointConfigured;
    private Boolean ossRegionConfigured;
    private Boolean ossBucketNameConfigured;
    private Boolean ossPublicUrlPrefixConfigured;
}
