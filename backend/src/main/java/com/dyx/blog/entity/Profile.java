package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人资料实体。
 */
@Data
@TableName("dyx_profile")
public class Profile {

    @TableId
    private Long id;
    private String siteTitle;
    private String heroTitle;
    private String heroSubtitle;
    private String heroConfig;
    private String aboutContent;
    private String educationExperience;
    private String workExperience;
    private String email;
    private String phone;
    private String wechat;
    private String githubUrl;
    private String contactMethods;
    private String avatarUrl;
    private String resumePdfUrl;
    private LocalDateTime updatedAt;
}
