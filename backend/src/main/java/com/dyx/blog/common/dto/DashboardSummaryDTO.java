package com.dyx.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 后台仪表盘摘要数据传输对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    /** 文章数量。 */
    private long postCount;

    /** 动态数量。 */
    private long momentCount;

    /** 荣誉数量。 */
    private long honorCount;

    /** 用户数量。 */
    private long userCount;

    /** 文章总浏览量。 */
    private long totalPostViews;

    /** 站点总访问量。 */
    private long totalSiteVisits;

    /** 近 7 日每日访问统计。 */
    private List<Map<String, Object>> dailySiteVisits;

    /** 设备类型分布。 */
    private List<Map<String, Object>> deviceTypeDistribution;

    /** 访问排行前 6 的页面。 */
    private List<Map<String, Object>> topVisitedPages;
}
