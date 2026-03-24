package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 媒体资源实体。
 */
@Data
@TableName("dyx_media")
public class Media {

    @TableId
    private Long id;
    private String originalName;
    private String fileName;
    private String fileUrl;
    private String mediaType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
