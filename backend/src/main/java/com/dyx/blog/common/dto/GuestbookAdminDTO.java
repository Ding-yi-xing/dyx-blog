package com.dyx.blog.common.dto;

import com.dyx.blog.entity.GuestbookMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 后台留言管理数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestbookAdminDTO {

    /** 留言页介绍文案。 */
    private String guestbookIntro;

    /** 留言列表。 */
    private List<GuestbookMessage> messages;
}
