package com.dyx.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台系统配置返回对象。
 * 用于聚合站点基础配置与 OSS 配置完成状态，返回给后台系统配置页渲染。
 */
@Data
public class AdminSystemConfigVo {

    /** 配置记录主键。 */
    private Long id;

    /** 当前启用的存储模式，如 local / oss。 */
    private String storageType;

    /** OSS 基础目录。 */
    private String ossBaseDir;

    /** 足迹模块眉标题。 */
    private String footprintEyebrow;

    /** 足迹模块主标题。 */
    private String footprintTitle;

    /** 足迹模块副标题。 */
    private String footprintSubtitle;

    /** 足迹模块说明文案。 */
    private String footprintDescription;

    /** 站点页脚版权文案。 */
    private String copyrightText;

    /** 页脚技术支持文案。 */
    private String techSupportText;

    /** 配置最后更新时间。 */
    private LocalDateTime updatedAt;

    /** OSS Endpoint 是否已配置。 */
    private Boolean ossEndpointConfigured;

    /** OSS Region 是否已配置。 */
    private Boolean ossRegionConfigured;

    /** OSS Bucket 名称是否已配置。 */
    private Boolean ossBucketNameConfigured;

    /** OSS 公网访问前缀是否已配置。 */
    private Boolean ossPublicUrlPrefixConfigured;

    /** 是否启用博客作为首页第三屏精选来源。 */
    private Boolean homeActivityEnablePosts;

    /** 是否启用动态作为首页第三屏精选来源。 */
    private Boolean homeActivityEnableMoments;

    /** 是否启用项目作为首页第三屏精选来源。 */
    private Boolean homeActivityEnableProjects;

    /** 是否启用作品作为首页第三屏精选来源。 */
    private Boolean homeActivityEnableWorks;

    /** 是否启用荣誉作为首页第三屏精选来源。 */
    private Boolean homeActivityEnableHonors;

    /** 首页第三屏最多展示总条数。 */
    private Integer homeActivityMaxItems;

    /** 首页第三屏单类型最多展示条数。 */
    private Integer homeActivityMaxItemsPerType;
}
