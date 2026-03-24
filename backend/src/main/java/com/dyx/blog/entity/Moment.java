package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态实体。
 */
@Data
@TableName("dyx_moment")
public class Moment {

    @TableId
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private LocalDateTime happenedAt;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
