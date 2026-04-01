package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言实体。
 * 保存前台留言板提交内容与后台审核使用的发布状态信息。
 */
@Data
@TableName("dyx_guestbook_message")
public class GuestbookMessage {

    /** 留言主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 留言正文。 */
    private String content;

    /** 发布状态，1 表示允许前台展示。 */
    private Integer published;

    /** 提交来源 IP。 */
    private String ipAddress;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
