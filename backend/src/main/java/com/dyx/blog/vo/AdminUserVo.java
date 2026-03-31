package com.dyx.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户返回对象。
 */
@Data
public class AdminUserVo {

    private Long id;
    private String username;
    private String displayName;
    private String role;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
