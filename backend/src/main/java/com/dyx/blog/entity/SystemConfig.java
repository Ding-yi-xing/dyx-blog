package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体。
 * 保存站点基础展示文案与媒体存储相关配置，供后台系统配置页统一维护。
 */
@Data
@TableName("dyx_system_config")
public class SystemConfig {

    /** 配置记录主键。 */
    @TableId
    private Long id;

    /** 当前启用的媒体存储类型，如 local 或 oss。 */
    private String storageType;

    /** OSS Endpoint 地址。 */
    private String ossEndpoint;

    /** OSS 所属地域编码。 */
    private String ossRegion;

    /** OSS Bucket 名称。 */
    private String ossBucketName;

    /** OSS 对外访问前缀，可覆盖默认拼接规则。 */
    private String ossPublicUrlPrefix;

    /** OSS 基础目录，上传对象键会拼接在该目录下。 */
    private String ossBaseDir;

    /** 是否启用 IP 实际地址查询。 */
    private Boolean ipLookupEnabled;

    /** IP 查询接口地址。 */
    private String ipLookupApiUrl;

    /** 足迹模块眉标题。 */
    private String footprintEyebrow;

    /** 足迹模块主标题。 */
    private String footprintTitle;

    /** 足迹模块副标题。 */
    private String footprintSubtitle;

    /** 足迹模块介绍文案。 */
    private String footprintDescription;

    /** 页脚版权文案。 */
    private String copyrightText;

    /** 页脚技术支持文案。 */
    private String techSupportText;

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

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
