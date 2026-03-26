package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站点访问日志实体。
 */
@Data
@TableName("dyx_site_visit_log")
public class SiteVisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String pageKey;
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String deviceName;
    private LocalDateTime createdAt;
}
