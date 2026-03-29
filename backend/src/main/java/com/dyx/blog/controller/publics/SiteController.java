package com.dyx.blog.controller.publics;

import com.dyx.blog.common.dto.GuestbookDataDTO;
import com.dyx.blog.common.dto.HomeDataDTO;
import com.dyx.blog.common.response.Result;
import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.GuestbookMessage;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.Work;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.service.SiteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 前台展示控制器。
 * 提供首页数据、博客、动态、项目及留言接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/site")
public class SiteController {

    private final SiteService dyxSiteService;

    /**
     * 获取首页聚合数据。
     *
     * @return 首页展示数据。
     */
    @GetMapping("/home")
    public Result<HomeDataDTO> getHomeData() {
        return Result.success(dyxSiteService.getHomeData());
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    @GetMapping("/profile")
    public Result<Profile> getProfile() {
        return Result.success(dyxSiteService.getProfile());
    }

    /**
     * 获取留言页数据。
     *
     * @return 留言页展示数据。
     */
    @GetMapping("/guestbook")
    public Result<GuestbookDataDTO> getGuestbookData() {
        return Result.success(dyxSiteService.getGuestbookData());
    }

    /**
     * 提交留言。
     *
     * @param message 留言对象。
     * @param request 当前请求。
     * @return 保存结果。
     */
    @PostMapping("/site/guestbook/messages")
    public Result<GuestbookMessage> createGuestbookMessage(@RequestBody GuestbookMessage message, HttpServletRequest request) {
        return Result.success(dyxSiteService.saveGuestbookMessage(message, request));
    }

    /**
     * 获取动态列表。
     *
     * @return 动态结果列表。
     */
    @GetMapping("/site/moments")
    public Result<List<Moment>> listMoments() {
        return Result.success(dyxSiteService.listMoments());
    }

    /**
     * 获取动态详情。
     *
     * @param id 动态主键。
     * @return 动态详情。
     */
    @GetMapping("/site/moments/{id}")
    public Result<Moment> getMomentDetail(@PathVariable Long id) {
        return Result.success(dyxSiteService.getMomentDetail(id));
    }

    /**
     * 获取项目经历列表。
     *
     * @return 项目经历结果列表。
     */
    @GetMapping("/site/projects")
    public Result<List<Project>> listProjects() {
        return Result.success(dyxSiteService.listProjects());
    }

    /**
     * 获取个人作品列表。
     *
     * @return 个人作品结果列表。
     */
    @GetMapping("/site/works")
    public Result<List<Work>> listWorks() {
        return Result.success(dyxSiteService.listWorks());
    }

    /**
     * 获取荣誉列表。
     *
     * @return 荣誉结果列表。
     */
    @GetMapping("/site/honors")
    public Result<List<Honor>> listHonors() {
        return Result.success(dyxSiteService.listHonors());
    }

    /**
     * 获取已发布足迹列表。
     *
     * @return 足迹结果列表。
     */
    @GetMapping("/site/footprints")
    public Result<List<Footprint>> listFootprints() {
        return Result.success(dyxSiteService.listFootprints());
    }

    /**
     * 记录页面访问。
     *
     * @param pageKey 页面标识。
     * @param request 当前请求。
     * @return 成功结果。
     */
    @PostMapping("/site/visit/{pageKey}")
    public Result<Void> recordSiteVisit(@PathVariable String pageKey, HttpServletRequest request) {
        dyxSiteService.recordSiteVisit(pageKey, request);
        return Result.success();
    }

    /**
     * 获取博客列表。
     *
     * @return 博客结果列表。
     */
    @GetMapping("/posts")
    public Result<List<Post>> listPosts() {
        return Result.success(dyxSiteService.listPosts());
    }

    /**
     * 获取博客详情。
     *
     * @param id 文章主键。
     * @return 文章详情结果。
     */
    @GetMapping("/posts/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        return Result.success(dyxSiteService.getPostDetail(id));
    }
}
