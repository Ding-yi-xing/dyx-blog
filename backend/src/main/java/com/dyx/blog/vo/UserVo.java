package com.dyx.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户视图对象。
 * 用于登录成功后向前端返回当前用户的基础身份信息。
 */
@Data
public class UserVo {

    /** 用户主键，序列化为字符串以避免前端精度丢失。 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 用户名。 */
    private String username;

    /** 显示名称。 */
    private String displayName;

    /** 角色编码。 */
    private String role;
}
