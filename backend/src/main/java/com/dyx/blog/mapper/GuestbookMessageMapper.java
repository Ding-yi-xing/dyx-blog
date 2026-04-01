package com.dyx.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dyx.blog.entity.GuestbookMessage;

/**
 * 留言 Mapper。
 * 提供留言内容与审核状态的基础持久化能力。
 */
public interface GuestbookMessageMapper extends BaseMapper<GuestbookMessage> {
}
