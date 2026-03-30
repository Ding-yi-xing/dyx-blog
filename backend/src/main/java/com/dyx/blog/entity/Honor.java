package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 荣誉实体。
 */
@Data
@TableName("dyx_honor")
public class Honor {

    @TableId
    private Long id;
    private String title;
    private String issuer;
    private String description;
    private String coverImage;
    private String imageUrls;
    private String attachmentUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime awardAt;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
