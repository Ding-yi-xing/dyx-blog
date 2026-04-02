package com.dyx.blog.common.dto;

import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 前台首页数据传输对象。
 * 仅聚合首页当前实际所需的 Hero、足迹与首页系统配置数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataDTO {

    /** 个人资料。 */
    private Profile profile;

    /** 全部已发布足迹。 */
    private List<Footprint> footprints;

    /** 首页相关系统配置。 */
    private Map<String, Object> systemConfig;
}
