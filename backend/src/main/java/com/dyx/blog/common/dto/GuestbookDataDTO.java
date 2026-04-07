package com.dyx.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前台留言页数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestbookDataDTO {

    /** 留言页介绍。 */
    private String guestbookIntro;

    /** 已发布的留言列表（已隐藏 IP 等敏感字段）。 */
    private List<GuestbookPublicMessageDTO> messages;
}
