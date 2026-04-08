package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人作品实体。
 * 保存关于我页面展示的作品卡片、图集与视频等多媒体信息。
 */
@Data
@TableName("dyx_work")
public class Work {

    /** 作品主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 作品标题。 */
    private String title;

    /** 作品简介。 */
    private String summary;

    /** 作品封面图。 */
    private String coverImage;

    /** 作品图集地址集合。 */
    private String imageUrls;

    /** 作品视频地址。 */
    private String videoUrl;

    /** 视频封面图地址。 */
    private String videoPoster;

    /** 作品外链地址。 */
    private String workLink;

    /** 获得时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime awardAt;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 发布状态，1 表示前台可见。 */
    private Integer published;

    /** 是否首页第三屏候选，1 表示参与精选。 */
    private Integer homeFeatured;

    /** 首页第三屏排序值，值越小越靠前。 */
    private Integer homeFeaturedOrder;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
