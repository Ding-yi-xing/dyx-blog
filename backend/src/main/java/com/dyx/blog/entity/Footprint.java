package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页足迹实体。
 * 对应首页地图中的足迹点位与其补充文案配置。
 */
@Data
@TableName("dyx_footprint")
public class Footprint {

    /** 足迹主键。 */
    @TableId
    private Long id;

    /** 城市名称，用于地图点位与卡片展示。 */
    private String cityName;

    /** 国家名称，兼容境外足迹场景。 */
    private String countryName;

    /** 省份或区域名称。 */
    private String regionName;

    /** 地图经度。 */
    private Double positionX;

    /** 地图纬度。 */
    private Double positionY;

    /** 到访时间，供前台时间轴与后台表单回显。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitedAt;

    /** 足迹描述文案。 */
    private String description;

    /** 重要程度，用于控制展示权重。 */
    private Integer importance;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 发布状态，1 表示对前台可见。 */
    private Integer published;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
