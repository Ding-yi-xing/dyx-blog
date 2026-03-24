package com.dyx.blog.controller.publics;

import com.dyx.blog.common.response.Result;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 前台站点控制器。
 * 提供公开访问的个人资料、文章和展示内容接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SiteController {

    private final SiteService dyxSiteService;

    /**
     * 获取首页聚合数据。
     *
     * @return 首页聚合结果。
     */
    @GetMapping("/site/home")
    public Result<Map<String, Object>> getHomeData() {
        return Result.success(dyxSiteService.getHomeData());
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料结果。
     */
    @GetMapping("/site/profile")
    public Result<Profile> getProfile() {
        return Result.success(dyxSiteService.getProfile());
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
     * 获取项目经历列表。
     *
     * @return 项目经历结果列表。
     */
    @GetMapping("/site/projects")
    public Result<List<Project>> listProjects() {
        return Result.success(dyxSiteService.listProjects());
    }

    /**
     * 获取照片列表。
     *
     * @return 照片结果列表。
     */
    @GetMapping("/site/photos")
    public Result<List<Photo>> listPhotos() {
        return Result.success(dyxSiteService.listPhotos());
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
