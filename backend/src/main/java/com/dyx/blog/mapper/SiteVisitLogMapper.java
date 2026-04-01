package com.dyx.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dyx.blog.entity.SiteVisitLog;

/**
 * 站点访问日志 Mapper。
 * 提供访问日志表的基础读写能力，供统计与后台筛选查询复用。
 */
public interface SiteVisitLogMapper extends BaseMapper<SiteVisitLog> {
}
