package com.dyx.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户返回对象。
 * 用于后台用户列表与编辑弹窗展示管理员账号基础信息。
 */
@Data
public class AdminUserVo {

    /** 用户主键。 */
    private Long id;

    /** 登录用户名。 */
    private String username;

    /** 后台展示名称。 */
    private String displayName;

    /** 角色编码，如 ADMIN / USER。 */
    private String role;

    /** 启用状态，1 表示启用。 */
    private Integer enabled;

    /** 账号创建时间。 */
    private LocalDateTime createdAt;

    /** 账号最后更新时间。 */
    private LocalDateTime updatedAt;
}
