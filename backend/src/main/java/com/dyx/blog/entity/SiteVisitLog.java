package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站点访问日志实体。
 * 记录公开页面访问时的页面标识、IP 与设备信息，供后台统计与审计使用。
 */
@Data
@TableName("dyx_site_visit_log")
public class SiteVisitLog {

    /** 日志主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 页面标识，如 home、about、blog-detail。 */
    private String pageKey;

    /** 访客 IP 地址。 */
    private String ipAddress;

    /** 浏览器 User-Agent 原始字符串。 */
    private String userAgent;

    /** 设备类型，如 desktop、mobile。 */
    private String deviceType;

    /** 设备名称或解析后的设备描述。 */
    private String deviceName;

    /** 访问记录创建时间。 */
    private LocalDateTime createdAt;
}
