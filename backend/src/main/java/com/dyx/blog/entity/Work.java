package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人作品实体。
 */
@Data
@TableName("dyx_work")
public class Work {

    @TableId
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String imageUrls;
    private String videoUrl;
    private String videoPoster;
    private String workLink;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
