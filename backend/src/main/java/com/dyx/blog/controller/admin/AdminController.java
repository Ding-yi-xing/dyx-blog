package com.dyx.blog.controller.admin;

import com.dyx.blog.common.dto.DashboardSummaryDTO;
import com.dyx.blog.common.dto.GuestbookAdminDTO;
import com.dyx.blog.common.dto.PageResult;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.response.Result;
import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.GuestbookMessage;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.SystemConfig;
import com.dyx.blog.entity.User;
import com.dyx.blog.entity.Work;
import com.dyx.blog.service.AdminService;
import com.dyx.blog.service.MediaService;
import com.dyx.blog.vo.AdminSystemConfigVo;
import com.dyx.blog.vo.AdminUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 后台管理控制器。
 * 提供仪表盘、内容管理、资料管理与媒体管理接口。
 * <p>
 * 该控制器统一承接后台页面的 CRUD 请求，并把具体业务校验、权限控制和缓存处理下沉到服务层。
 * 控制器层仅负责参数接收、路径变量回填和统一响应包装。
 * </p>
 *
 * @author Dyx
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dyx-manager")
public class AdminController {

    private final AdminService dyxAdminService;
    private final MediaService dyxMediaService;

    /**
     * 获取后台仪表盘摘要。
     *
     * @return 摘要数据。
     */
    @GetMapping("/dashboard/summary")
    public Result<DashboardSummaryDTO> getDashboardSummary() {
        return Result.success(dyxAdminService.getDashboardSummary());
    }

    /**
     * 获取访问日志列表（分页）。
     *
     * @return 包含访问日志记录及分页信息的结果。
     */
    @GetMapping("/visit-logs")
    public Result<PageResult<Map<String, Object>>> listVisitLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String pageKey,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return Result.success(
                dyxAdminService.listVisitLogs(startTime, endTime, pageKey, deviceType, ipAddress, page, pageSize));
    }

    /**
     * 删除访问日志。
     *
     * @param id 访问日志主键。
     * @return 删除结果。
     */
    @DeleteMapping("/visit-logs/{id}")
    public Result<Void> deleteVisitLog(@PathVariable Long id) {
        dyxAdminService.deleteVisitLog(id);
        return Result.success();
    }

    /**
     * 批量删除访问日志。
     *
     * @param ids 访问日志主键列表。
     * @return 删除结果。
     */
    @PostMapping("/visit-logs/batch-delete")
    public Result<Void> deleteVisitLogs(@RequestBody List<Long> ids) {
        dyxAdminService.deleteVisitLogs(ids);
        return Result.success();
    }

    /**
     * 获取留言管理数据。
     *
     * @return 留言管理数据。
     */
    @GetMapping("/guestbook")
    public Result<GuestbookAdminDTO> getGuestbookAdminData() {
        return Result.success(dyxAdminService.getGuestbookAdminData());
    }

    /**
     * 更新留言页介绍。
     *
     * @param payload 包含介绍文案的请求体。
     * @return 保存结果。
     */
    @PutMapping("/guestbook/intro")
    public Result<Profile> updateGuestbookIntro(@RequestBody Map<String, String> payload) {
        return Result
                .success(dyxAdminService.saveGuestbookIntro(payload == null ? null : payload.get("guestbookIntro")));
    }

    /**
     * 更新留言。
     *
     * @param id      留言主键。
     * @param message 留言对象。
     * @return 保存结果。
     */
    @PutMapping("/guestbook/messages/{id}")
    public Result<GuestbookMessage> updateGuestbookMessage(@PathVariable Long id,
            @RequestBody GuestbookMessage message) {
        return Result.success(dyxAdminService.updateGuestbookMessage(id, message));
    }

    /**
     * 删除留言。
     *
     * @param id 留言主键。
     * @return 删除结果。
     */
    @DeleteMapping("/guestbook/messages/{id}")
    public Result<Void> deleteGuestbookMessage(@PathVariable Long id) {
        dyxAdminService.deleteGuestbookMessage(id);
        return Result.success();
    }

    /**
     * 批量删除留言。
     *
     * @param ids 留言主键列表。
     * @return 删除结果。
     */
    @PostMapping("/guestbook/messages/batch-delete")
    public Result<Void> deleteGuestbookMessages(@RequestBody List<Long> ids) {
        dyxAdminService.deleteGuestbookMessages(ids);
        return Result.success();
    }

    /**
     * 获取文章列表。
     *
     * @return 文章列表结果。
     */
    @GetMapping("/posts")
    public Result<List<Post>> listPosts() {
        return Result.success(dyxAdminService.listPosts());
    }

    /**
     * 获取单篇文章详情。
     *
     * @param id 文章主键。
     * @return 文章详情结果。
     */
    @GetMapping("/posts/{id}")
    public Result<Post> getPost(@PathVariable Long id) {
        return Result.success(dyxAdminService.getPost(id));
    }

    /**
     * 新增文章。
     *
     * @param post 文章对象。
     * @return 保存结果。
     */
    @PostMapping("/posts")
    public Result<Post> createPost(@RequestBody Post post) {
        return Result.success(dyxAdminService.savePost(post));
    }

    /**
     * 更新文章。
     *
     * @param id   文章主键。
     * @param post 文章对象。
     * @return 保存结果。
     */
    @PutMapping("/posts/{id}")
    public Result<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        post.setId(id);
        return Result.success(dyxAdminService.savePost(post));
    }

    /**
     * 删除文章。
     *
     * @param id 文章主键。
     * @return 删除结果。
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        dyxAdminService.deletePost(id);
        return Result.success();
    }

    /**
     * 批量删除文章。
     *
     * @param ids 文章主键列表。
     * @return 删除结果。
     */
    @PostMapping("/posts/batch-delete")
    public Result<Void> deletePosts(@RequestBody List<Long> ids) {
        dyxAdminService.deletePosts(ids);
        return Result.success();
    }

    /**
     * 获取动态列表。
     *
     * @return 动态列表结果。
     */
    @GetMapping("/moments")
    public Result<List<Moment>> listMoments() {
        return Result.success(dyxAdminService.listMoments());
    }

    /**
     * 新增动态。
     *
     * @param moment 动态对象。
     * @return 保存结果。
     */
    @PostMapping("/moments")
    public Result<Moment> createMoment(@RequestBody Moment moment) {
        return Result.success(dyxAdminService.saveMoment(moment));
    }

    /**
     * 更新动态。
     *
     * @param id     动态主键。
     * @param moment 动态对象。
     * @return 保存结果。
     */
    @PutMapping("/moments/{id}")
    public Result<Moment> updateMoment(@PathVariable Long id, @RequestBody Moment moment) {
        moment.setId(id);
        return Result.success(dyxAdminService.saveMoment(moment));
    }

    /**
     * 删除动态。
     *
     * @param id 动态主键。
     * @return 删除结果。
     */
    @DeleteMapping("/moments/{id}")
    public Result<Void> deleteMoment(@PathVariable Long id) {
        dyxAdminService.deleteMoment(id);
        return Result.success();
    }

    /**
     * 批量删除动态。
     *
     * @param ids 动态主键列表。
     * @return 删除结果。
     */
    @PostMapping("/moments/batch-delete")
    public Result<Void> deleteMoments(@RequestBody List<Long> ids) {
        dyxAdminService.deleteMoments(ids);
        return Result.success();
    }

    /**
     * 获取项目经历列表。
     *
     * @return 项目经历结果列表。
     */
    @GetMapping("/projects")
    public Result<List<Project>> listProjects() {
        return Result.success(dyxAdminService.listProjects());
    }

    /**
     * 新增项目经历。
     *
     * @param project 项目经历对象。
     * @return 保存结果。
     */
    @PostMapping("/projects")
    public Result<Project> createProject(@RequestBody Project project) {
        return Result.success(dyxAdminService.saveProject(project));
    }

    /**
     * 更新项目经历。
     *
     * @param id      项目经历主键。
     * @param project 项目经历对象。
     * @return 保存结果。
     */
    @PutMapping("/projects/{id}")
    public Result<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        return Result.success(dyxAdminService.saveProject(project));
    }

    /**
     * 删除项目经历。
     *
     * @param id 项目经历主键。
     * @return 删除结果。
     */
    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        dyxAdminService.deleteProject(id);
        return Result.success();
    }

    /**
     * 批量删除项目经历。
     *
     * @param ids 项目经历主键列表。
     * @return 删除结果。
     */
    @PostMapping("/projects/batch-delete")
    public Result<Void> deleteProjects(@RequestBody List<Long> ids) {
        dyxAdminService.deleteProjects(ids);
        return Result.success();
    }

    /**
     * 获取个人作品列表。
     *
     * @return 作品结果列表。
     */
    @GetMapping("/works")
    public Result<List<Work>> listWorks() {
        return Result.success(dyxAdminService.listWorks());
    }

    /**
     * 新增个人作品。
     *
     * @param work 作品对象。
     * @return 保存结果。
     */
    @PostMapping("/works")
    public Result<Work> createWork(@RequestBody Work work) {
        return Result.success(dyxAdminService.saveWork(work));
    }

    /**
     * 更新个人作品。
     *
     * @param id   作品主键。
     * @param work 作品对象。
     * @return 保存结果。
     */
    @PutMapping("/works/{id}")
    public Result<Work> updateWork(@PathVariable Long id, @RequestBody Work work) {
        work.setId(id);
        return Result.success(dyxAdminService.saveWork(work));
    }

    /**
     * 删除个人作品。
     *
     * @param id 作品主键。
     * @return 删除结果。
     */
    @DeleteMapping("/works/{id}")
    public Result<Void> deleteWork(@PathVariable Long id) {
        dyxAdminService.deleteWork(id);
        return Result.success();
    }

    /**
     * 批量删除个人作品。
     *
     * @param ids 作品主键列表。
     * @return 删除结果。
     */
    @PostMapping("/works/batch-delete")
    public Result<Void> deleteWorks(@RequestBody List<Long> ids) {
        dyxAdminService.deleteWorks(ids);
        return Result.success();
    }

    /**
     * 获取荣誉列表。
     *
     * @return 荣誉结果列表。
     */
    @GetMapping("/honors")
    public Result<List<Honor>> listHonors() {
        return Result.success(dyxAdminService.listHonors());
    }

    /**
     * 新增荣誉。
     *
     * @param honor 荣誉对象。
     * @return 保存结果。
     */
    @PostMapping("/honors")
    public Result<Honor> createHonor(@RequestBody Honor honor) {
        return Result.success(dyxAdminService.saveHonor(honor));
    }

    /**
     * 更新荣誉。
     *
     * @param id    荣誉主键。
     * @param honor 荣誉对象。
     * @return 保存结果。
     */
    @PutMapping("/honors/{id}")
    public Result<Honor> updateHonor(@PathVariable Long id, @RequestBody Honor honor) {
        honor.setId(id);
        return Result.success(dyxAdminService.saveHonor(honor));
    }

    /**
     * 删除荣誉。
     *
     * @param id 荣誉主键。
     * @return 删除结果。
     */
    @DeleteMapping("/honors/{id}")
    public Result<Void> deleteHonor(@PathVariable Long id) {
        dyxAdminService.deleteHonor(id);
        return Result.success();
    }

    /**
     * 批量删除荣誉。
     *
     * @param ids 荣誉主键列表。
     * @return 删除结果。
     */
    @PostMapping("/honors/batch-delete")
    public Result<Void> deleteHonors(@RequestBody List<Long> ids) {
        dyxAdminService.deleteHonors(ids);
        return Result.success();
    }

    /**
     * 获取首页足迹列表。
     *
     * @return 足迹结果列表。
     */
    @GetMapping("/footprints")
    public Result<List<Footprint>> listFootprints() {
        return Result.success(dyxAdminService.listFootprints());
    }

    /**
     * 新增首页足迹。
     *
     * @param footprint 足迹对象。
     * @return 保存结果。
     */
    @PostMapping("/footprints")
    public Result<Footprint> createFootprint(@RequestBody Footprint footprint) {
        return Result.success(dyxAdminService.saveFootprint(footprint));
    }

    /**
     * 更新首页足迹。
     *
     * @param id        足迹主键。
     * @param footprint 足迹对象。
     * @return 保存结果。
     */
    @PutMapping("/footprints/{id}")
    public Result<Footprint> updateFootprint(@PathVariable Long id, @RequestBody Footprint footprint) {
        footprint.setId(id);
        return Result.success(dyxAdminService.saveFootprint(footprint));
    }

    /**
     * 删除首页足迹。
     *
     * @param id 足迹主键。
     * @return 删除结果。
     */
    @DeleteMapping("/footprints/{id}")
    public Result<Void> deleteFootprint(@PathVariable Long id) {
        dyxAdminService.deleteFootprint(id);
        return Result.success();
    }

    /**
     * 批量删除首页足迹。
     *
     * @param ids 足迹主键列表。
     * @return 删除结果。
     */
    @PostMapping("/footprints/batch-delete")
    public Result<Void> deleteFootprints(@RequestBody List<Long> ids) {
        dyxAdminService.deleteFootprints(ids);
        return Result.success();
    }

    /**
     * 获取首页横幅配置。
     *
     * @return 首页横幅结果。
     */
    @GetMapping("/profile/hero")
    public Result<Profile> getHeroProfile() {
        return Result.success(dyxAdminService.getHeroProfile());
    }

    /**
     * 更新首页横幅配置。
     *
     * @param profile 首页横幅对象。
     * @return 保存结果。
     */
    @PutMapping("/profile/hero")
    public Result<Profile> updateHeroProfile(@RequestBody Profile profile) {
        return Result.success(dyxAdminService.saveHeroProfile(profile));
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料结果。
     */
    @GetMapping("/profile")
    public Result<Profile> getProfile() {
        return Result.success(dyxAdminService.getProfile());
    }

    /**
     * 更新个人资料。
     *
     * @param profile 个人资料对象。
     * @return 保存结果。
     */
    @PutMapping("/profile")
    public Result<Profile> updateProfile(@RequestBody Profile profile) {
        return Result.success(dyxAdminService.saveProfile(profile));
    }

    /**
     * 获取系统配置。
     *
     * @return 系统配置结果。
     */
    @GetMapping("/system-config")
    public Result<AdminSystemConfigVo> getSystemConfig() {
        return Result.success(dyxAdminService.getAdminSystemConfig());
    }

    /**
     * 更新系统配置。
     *
     * @param systemConfig 系统配置对象。
     * @return 保存结果。
     */
    @PutMapping("/system-config")
    public Result<AdminSystemConfigVo> updateSystemConfig(@RequestBody SystemConfig systemConfig) {
        return Result.success(dyxAdminService.saveSystemConfig(systemConfig));
    }

    /**
     * 获取用户列表。
     *
     * @return 用户列表结果。
     */
    @GetMapping("/users")
    public Result<List<AdminUserVo>> listUsers() {
        return Result.success(dyxAdminService.listUsers());
    }

    /**
     * 新增用户。
     *
     * @param user 用户对象。
     * @return 保存结果。
     */
    @PostMapping("/users")
    public Result<AdminUserVo> createUser(@RequestBody User user) {
        return Result.success(dyxAdminService.saveUser(user));
    }

    /**
     * 更新用户。
     *
     * @param id   用户主键。
     * @param user 用户对象。
     * @return 保存结果。
     */
    @PutMapping("/users/{id}")
    public Result<AdminUserVo> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return Result.success(dyxAdminService.saveUser(user));
    }

    /**
     * 删除用户。
     *
     * @param id 用户主键。
     * @return 删除结果。
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        dyxAdminService.deleteUser(id);
        return Result.success();
    }

    /**
     * 获取媒体列表。
     *
     * @return 媒体结果列表。
     */
    @GetMapping("/media")
    public Result<?> listMedia() {
        return Result.success(dyxMediaService.listAll());
    }

    /**
     * 代理读取媒体文件，供同源裁剪场景使用。
     *
     * @param fileUrl 媒体地址。
     * @return 媒体文件响应。
     */
    @GetMapping("/media/content")
    public ResponseEntity<byte[]> proxyMedia(@RequestParam String fileUrl,
                                             @RequestParam(required = false) String range,
                                             @org.springframework.web.bind.annotation.RequestHeader(value = "Range", required = false) String requestRange) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new BusinessException("fileUrl 不能为空");
        }
        String resolvedRange = requestRange != null ? requestRange : range;
        return dyxMediaService.proxyMedia(fileUrl, resolvedRange);
    }

    /**
     * 上传媒体文件。
     *
     * @param file 上传文件。
     * @return 上传结果。
     */
    @PostMapping("/media/upload")
    public Result<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        return Result.success(dyxMediaService.upload(file));
    }

    /**
     * 删除媒体文件。
     *
     * @param id 媒体主键。
     * @return 删除结果。
     */
    @DeleteMapping("/media/{id}")
    public Result<Void> deleteMedia(@PathVariable Long id) {
        dyxMediaService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除媒体文件。
     *
     * @param ids 媒体主键列表。
     * @return 删除结果。
     */
    @PostMapping("/media/batch-delete")
    public Result<Void> deleteMediaBatch(@RequestBody List<Long> ids) {
        dyxMediaService.deleteBatchByIds(ids);
        return Result.success();
    }

    /**
     * 导入 uploads 目录中已存在的文件。
     *
     * @return 导入数量。
     */
    @PostMapping("/media/import-existing")
    public Result<Integer> importExistingMedia() {
        return Result.success(dyxMediaService.importExistingFiles());
    }
}
