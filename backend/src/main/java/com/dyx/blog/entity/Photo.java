package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 照片实体。
 */
@Data
@TableName("dyx_photo")
public class Photo {

    @TableId
    private Long id;
    private String title;
    private String imageUrl;
    private String description;
    private LocalDateTime shotAt;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
