package com.dyx.blog.service;

import com.dyx.blog.common.dto.GuestbookDataDTO;
import com.dyx.blog.common.dto.HomeDataDTO;
import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.GuestbookMessage;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.Work;
import jakarta.servlet.http.HttpServletRequest;

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
    HomeDataDTO getHomeData();

    /**
     * 获取个人资料信息。
     *
     * @return 个人资料。
     */
    Profile getProfile();

    /**
     * 获取留言页数据。
     *
     * @return 留言页数据。
     */
    GuestbookDataDTO getGuestbookData();

    /**
     * 提交留言。
     *
     * @param message 留言内容。
     * @param request 当前请求。
     * @return 保存后的留言。
     */
    GuestbookMessage saveGuestbookMessage(GuestbookMessage message, HttpServletRequest request);

    /**
     * 获取已发布文章列表。
     *
     * @return 文章列表。
     */
    List<Post> listPosts(Integer page, Integer pageSize);

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
     * 获取已发布动态详情。
     *
     * @param id 动态主键。
     * @return 动态详情。
     */
    Moment getMomentDetail(Long id);

    /**
     * 获取已发布项目经历列表。
     *
     * @return 项目经历列表。
     */
    List<Project> listProjects();

    /**
     * 获取已发布个人作品列表。
     *
     * @return 作品列表。
     */
    List<Work> listWorks();

    /**
     * 获取已发布荣誉列表。
     *
     * @return 荣誉列表。
     */
    List<Honor> listHonors();

    /**
     * 获取已发布首页足迹列表。
     *
     * @return 足迹列表。
     */
    List<Footprint> listFootprints();

    /**
     * 记录公开页面访问。
     *
     * @param pageKey 页面标识。
     * @param request 当前请求。
     */
    void recordSiteVisit(String pageKey, HttpServletRequest request);
}
