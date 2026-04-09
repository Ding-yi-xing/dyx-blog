package com.dyx.blog.controller.publics;

import com.dyx.blog.common.dto.GuestbookDataDTO;
import com.dyx.blog.common.dto.HomeDataDTO;
import com.dyx.blog.common.dto.HomeDeferredDataDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
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
     * 获取首页延迟数据。
     *
     * @return 首页第二、第三屏所需数据。
     */
    @GetMapping("/home/deferred")
    public Result<HomeDeferredDataDTO> getHomeDeferredData() {
        return Result.success(dyxSiteService.getHomeDeferredData());
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
     * 接收公开留言并保存到留言数据表。
     * <p>
     * 该方法会将留言内容与当前请求上下文一并传入站点服务，由服务层负责完成内容校验、文本清洗、IP 解析和默认状态填充。
     * 若留言内容为空、超长或不满足业务约束，会由服务层抛出业务异常并交由全局异常处理器统一返回。
     * </p>
     *
     * @param message 留言请求对象，主要包含访客提交的留言内容。
     * @param request 当前 HTTP 请求，用于提取来源 IP、请求头等上下文信息。
     * @return 包含保存后留言数据的统一响应结果。
     * @throws BusinessException 当留言内容为空、长度超限或保存过程不满足业务约束时抛出。
     * @author Dyx
     */
    @PostMapping("/guestbook/messages")
    public Result<GuestbookMessage> createGuestbookMessage(@RequestBody GuestbookMessage message,
            HttpServletRequest request) {
        return Result.success(dyxSiteService.saveGuestbookMessage(message, request));
    }

    /**
     * 获取动态列表。
     *
     * @return 动态结果列表。
     */
    @GetMapping("/moments")
    public Result<List<Moment>> listMoments() {
        return Result.success(dyxSiteService.listMoments());
    }

    /**
     * 获取动态详情。
     *
     * @param id 动态主键。
     * @return 动态详情。
     */
    @GetMapping("/moments/{id}")
    public Result<Moment> getMomentDetail(@PathVariable Long id) {
        return Result.success(dyxSiteService.getMomentDetail(id));
    }

    /**
     * 获取项目经历列表。
     *
     * @return 项目经历结果列表。
     */
    @GetMapping("/projects")
    public Result<List<Project>> listProjects() {
        return Result.success(dyxSiteService.listProjects());
    }

    /**
     * 获取个人作品列表。
     *
     * @return 个人作品结果列表。
     */
    @GetMapping("/works")
    public Result<List<Work>> listWorks() {
        return Result.success(dyxSiteService.listWorks());
    }

    /**
     * 获取荣誉列表。
     *
     * @return 荣誉结果列表。
     */
    @GetMapping("/honors")
    public Result<List<Honor>> listHonors() {
        return Result.success(dyxSiteService.listHonors());
    }

    /**
     * 获取已发布足迹列表。
     *
     * @return 足迹结果列表。
     */
    @GetMapping("/footprints")
    public Result<List<Footprint>> listFootprints() {
        return Result.success(dyxSiteService.listFootprints());
    }

    /**
     * 记录公开页面访问行为。
     * <p>
     * 前端在页面展示阶段主动调用该接口上报访问事件，服务端会结合页面标识与请求头信息统计页面访问量，并写入访问日志。
     * 该接口本身不返回业务数据，仅返回统一成功结果。
     * </p>
     *
     * @param pageKey 页面标识，用于区分首页、关于我、简历、博客详情等访问来源。
     * @param request 当前 HTTP 请求，用于解析 IP、设备类型与 User-Agent 等访问信息。
     * @return 空数据的统一成功响应结果。
     * @throws BusinessException 当页面标识非法或访问记录处理过程中触发业务校验时抛出。
     * @author Dyx
     */
    @PostMapping("/visit/{pageKey}")
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
    public Result<List<Post>> listPosts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(dyxSiteService.listPosts(page, pageSize));
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
