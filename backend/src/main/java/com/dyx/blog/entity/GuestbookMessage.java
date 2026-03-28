package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言实体。
 */
@Data
@TableName("dyx_guestbook_message")
public class GuestbookMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private Integer published;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
