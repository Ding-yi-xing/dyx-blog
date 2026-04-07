package com.dyx.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 前台公开留言数据传输对象。
 * <p>
 * 仅保留前台展示所需的基础字段，不包含 IP 等敏感信息。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestbookPublicMessageDTO {

    /** 留言主键。 */
    private Long id;

    /** 留言正文。 */
    private String content;

    /** 发布状态，1 表示允许前台展示。 */
    private Integer published;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
