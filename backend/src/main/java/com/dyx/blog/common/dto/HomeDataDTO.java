package com.dyx.blog.common.dto;

import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 前台首页数据传输对象。
 * 聚合首页首屏、最新内容、精选项目、荣誉与足迹地图所需的数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataDTO {

    /** 个人资料。 */
    private Profile profile;

    /** 最新文章（限 3 篇）。 */
    private List<Post> latestPosts;

    /** 最新动态（限 3 条）。 */
    private List<Moment> latestMoments;

    /** 精选项目经历（限 3 个）。 */
    private List<Project> featuredProjects;

    /** 最新荣誉（限 3 项）。 */
    private List<Honor> latestHonors;

    /** 全部已发布足迹。 */
    private List<Footprint> footprints;

    /** 首页相关系统配置。 */
    private Map<String, Object> systemConfig;
}
