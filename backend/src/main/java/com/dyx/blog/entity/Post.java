package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体。
 * 保存博客文章正文、封面、分类标签与发布状态等核心内容信息。
 */
@Data
@TableName("dyx_post")
public class Post {

    /** 文章主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 文章标题。 */
    private String title;

    /** 文章摘要。 */
    private String summary;

    /** 文章正文内容。 */
    private String content;

    /** 文章封面图。 */
    private String coverImage;

    /** 文章分类。 */
    private String category;

    /** 标签集合文本。 */
    private String tags;

    /** 发布状态，1 表示前台可见。 */
    private Integer published;

    /** 浏览量。 */
    private Integer viewCount;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
