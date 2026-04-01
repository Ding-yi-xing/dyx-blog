package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态实体。
 * 保存站点最新动态时间线中的图文内容与排序发布信息。
 */
@Data
@TableName("dyx_moment")
public class Moment {

    /** 动态主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 动态标题。 */
    private String title;

    /** 动态正文内容。 */
    private String content;

    /** 动态封面图。 */
    private String coverImage;

    /** 动态图集地址集合。 */
    private String imageUrls;

    /** 动态发生时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenedAt;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 发布状态，1 表示前台可见。 */
    private Integer published;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
