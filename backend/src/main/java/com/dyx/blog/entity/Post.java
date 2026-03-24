package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体。
 */
@Data
@TableName("dyx_post")
public class Post {

    @TableId
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String category;
    private String tags;
    private Integer published;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
