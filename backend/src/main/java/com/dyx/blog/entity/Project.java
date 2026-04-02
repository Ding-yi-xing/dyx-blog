package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目经历实体。
 * 保存关于我与首页精选项目模块使用的项目经验信息。
 */
@Data
@TableName("dyx_project")
public class Project {

    /** 项目主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 项目名称。 */
    private String name;

    /** 在项目中的角色名称。 */
    private String roleName;

    /** 项目描述。 */
    private String description;

    /** 技术栈说明。 */
    private String techStack;

    /** 项目演示或仓库链接。 */
    private String projectLink;

    /** 项目封面图。 */
    private String coverImage;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 发布状态，1 表示前台可见。 */
    private Integer published;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
