package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目经历实体。
 */
@Data
@TableName("dyx_project")
public class Project {

    @TableId
    private Long id;
    private String name;
    private String roleName;
    private String description;
    private String techStack;
    private String projectLink;
    private String coverImage;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
