package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页足迹实体。
 */
@Data
@TableName("dyx_footprint")
public class Footprint {

    @TableId
    private Long id;
    private String cityName;
    private String countryName;
    private String regionName;
    private Double positionX;
    private Double positionY;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime visitedAt;
    private String description;
    private Integer importance;
    private Integer sortOrder;
    private Integer published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
