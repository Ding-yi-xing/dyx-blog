package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人资料实体。
 * 聚合首页、关于我、简历、留言页等前台模块共用的站点主人资料。
 */
@Data
@TableName("dyx_profile")
public class Profile {

    /** 资料主键。 */
    @TableId
    private Long id;

    /** 站点标题。 */
    private String siteTitle;

    /** 首页 Hero 主标题。 */
    private String heroTitle;

    /** 首页 Hero 副标题。 */
    private String heroSubtitle;

    /** 首页 Hero 扩展配置 JSON。 */
    private String heroConfig;

    /** 关于我正文内容。 */
    private String aboutContent;

    /** 教育经历富文本或结构化文本。 */
    private String educationExperience;

    /** 工作经历富文本或结构化文本。 */
    private String workExperience;

    /** 联系邮箱。 */
    private String email;

    /** 联系电话。 */
    private String phone;

    /** 微信号。 */
    private String wechat;

    /** GitHub 主页地址。 */
    private String githubUrl;

    /** 联系方式扩展配置，供前台按类型渲染跳转。 */
    private String contactMethods;

    /** 头像地址。 */
    private String avatarUrl;

    /** PDF 简历地址。 */
    private String resumePdfUrl;

    /** 留言页介绍文案。 */
    private String guestbookIntro;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
