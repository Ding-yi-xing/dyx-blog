package com.dyx.blog.service;

import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 前台站点服务接口。
 */
public interface SiteService {

    /**
     * 获取首页聚合数据。
     *
     * @return 首页展示数据。
     */
    Map<String, Object> getHomeData();

    /**
     * 获取个人资料信息。
     *
     * @return 个人资料。
     */
    Profile getProfile();

    /**
     * 获取已发布文章列表。
     *
     * @return 文章列表。
     */
    List<Post> listPosts();

    /**
     * 获取文章详情。
     *
     * @param id 文章主键。
     * @return 文章详情。
     */
    Post getPostDetail(Long id);

    /**
     * 获取已发布动态列表。
     *
     * @return 动态列表。
     */
    List<Moment> listMoments();

    /**
     * 获取已发布项目经历列表。
     *
     * @return 项目经历列表。
     */
    List<Project> listProjects();

    /**
     * 获取已发布荣誉列表。
     *
     * @return 荣誉列表。
     */
    List<Honor> listHonors();

    /**
     * 获取已发布照片列表。
     *
     * @return 照片列表。
     */
    List<Photo> listPhotos();
}
