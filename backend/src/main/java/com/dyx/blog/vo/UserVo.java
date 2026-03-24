package com.dyx.blog.vo;

import lombok.Data;

/**
 * 用户视图对象。
 */
@Data
public class UserVo {

    /** 用户主键。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 显示名称。 */
    private String displayName;

    /** 角色。 */
    private String role;
}
