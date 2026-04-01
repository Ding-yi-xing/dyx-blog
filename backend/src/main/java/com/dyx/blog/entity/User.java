package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体。
 * 保存后台登录账号、角色与启用状态等认证基础信息。
 */
@Data
@TableName("dyx_user")
public class User {

    /** 用户主键。 */
    @TableId
    private Long id;

    /** 登录用户名。 */
    private String username;

    /** BCrypt 等方式加密后的密码摘要。 */
    private String password;

    /** 后台展示名称。 */
    private String displayName;

    /** 角色编码，如 ADMIN / USER。 */
    private String role;

    /** 启用状态，1 表示启用。 */
    private Integer enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
