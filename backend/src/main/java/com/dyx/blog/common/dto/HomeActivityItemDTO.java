package com.dyx.blog.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 首页第三屏精选条目 DTO。
 * 统一封装博客、动态、项目、作品与荣誉在首页第三屏展示所需的关键字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeActivityItemDTO {

    /** 内容类型：POST / MOMENT / PROJECT / WORK / HONOR。 */
    private String type;

    /** 原始内容主键。 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long refId;

    /** 展示标题。 */
    private String title;

    /** 展示摘要或简短说明。 */
    private String summary;

    /** 可选封面图地址。 */
    private String coverImage;

    /** 用于排序的高亮时间（如发布时间、更新时间或获奖时间）。 */
    private LocalDateTime highlightTime;
}
