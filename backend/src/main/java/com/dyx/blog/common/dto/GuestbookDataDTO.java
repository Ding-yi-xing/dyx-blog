package com.dyx.blog.common.dto;

import com.dyx.blog.entity.GuestbookMessage;
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

    /** 已发布的留言列表。 */
    private List<GuestbookMessage> messages;
}
