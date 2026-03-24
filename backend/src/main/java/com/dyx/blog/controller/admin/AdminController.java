package com.dyx.blog.controller.admin;

import com.dyx.blog.common.response.Result;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.User;
import com.dyx.blog.service.AdminService;
import com.dyx.blog.service.MediaService;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Map;

/**
 * 后台管理控制器。
 * 提供仪表盘、内容管理、资料管理与媒体管理接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService dyxAdminService;
    private final MediaService dyxMediaService;

    /**
     * 获取后台仪表盘摘要。
     *
     * @return 摘要数据。
     */
    @GetMapping("/dashboard/summary")
    public Result<Map<String, Object>> getDashboardSummary() {
        return Result.success(dyxAdminService.getDashboardSummary());
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
     * @param id 文章主键。
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
     * @param id 动态主键。
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
     * @param id 项目经历主键。
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
     * 获取照片列表。
     *
     * @return 照片结果列表。
     */
    @GetMapping("/photos")
    public Result<List<Photo>> listPhotos() {
        return Result.success(dyxAdminService.listPhotos());
    }

    /**
     * 新增照片。
     *
     * @param photo 照片对象。
     * @return 保存结果。
     */
    @PostMapping("/photos")
    public Result<Photo> createPhoto(@RequestBody Photo photo) {
        return Result.success(dyxAdminService.savePhoto(photo));
    }

    /**
     * 更新照片。
     *
     * @param id 照片主键。
     * @param photo 照片对象。
     * @return 保存结果。
     */
    @PutMapping("/photos/{id}")
    public Result<Photo> updatePhoto(@PathVariable Long id, @RequestBody Photo photo) {
        photo.setId(id);
        return Result.success(dyxAdminService.savePhoto(photo));
    }

    /**
     * 删除照片。
     *
     * @param id 照片主键。
     * @return 删除结果。
     */
    @DeleteMapping("/photos/{id}")
    public Result<Void> deletePhoto(@PathVariable Long id) {
        dyxAdminService.deletePhoto(id);
        return Result.success();
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
     * 获取媒体列表。
     *
     * @return 媒体结果列表。
     */
    @GetMapping("/media")
    public Result<?> listMedia() {
        return Result.success(dyxMediaService.listAll());
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
     * 获取后台用户列表。
     *
     * @return 用户结果列表。
     */
    @GetMapping("/users")
    public Result<List<User>> listUsers() {
        return Result.success(dyxAdminService.listUsers());
    }
}
