package com.dyx.blog.common.dto;

import com.dyx.blog.entity.Footprint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 首页延迟数据传输对象。
 * 聚合第二屏足迹、首页系统配置与第三屏精选内容。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDeferredDataDTO {

    /** 全部已发布足迹。 */
    private List<Footprint> footprints;

    /** 首页相关系统配置。 */
    private Map<String, Object> systemConfig;

    /** 首页第三屏精选条目列表。 */
    private List<HomeActivityItemDTO> featuredItems;
}
