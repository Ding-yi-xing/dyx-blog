package com.dyx.blog.service;

import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 后台管理服务接口。
 */
public interface AdminService {

    /**
     * 获取后台仪表盘摘要。
     *
     * @return 统计摘要。
     */
    Map<String, Object> getDashboardSummary();

    /**
     * 查询全部文章。
     *
     * @return 文章列表。
     */
    List<Post> listPosts();

    /**
     * 保存文章。
     *
     * @param post 文章对象。
     * @return 保存后的文章。
     */
    Post savePost(Post post);

    /**
     * 删除文章。
     *
     * @param id 文章主键。
     */
    void deletePost(Long id);

    /**
     * 查询全部动态。
     *
     * @return 动态列表。
     */
    List<Moment> listMoments();

    /**
     * 保存动态。
     *
     * @param moment 动态对象。
     * @return 保存后的动态。
     */
    Moment saveMoment(Moment moment);

    /**
     * 删除动态。
     *
     * @param id 动态主键。
     */
    void deleteMoment(Long id);

    /**
     * 查询全部项目经历。
     *
     * @return 项目经历列表。
     */
    List<Project> listProjects();

    /**
     * 保存项目经历。
     *
     * @param project 项目经历对象。
     * @return 保存后的项目经历。
     */
    Project saveProject(Project project);

    /**
     * 删除项目经历。
     *
     * @param id 项目经历主键。
     */
    void deleteProject(Long id);

    /**
     * 查询全部荣誉。
     *
     * @return 荣誉列表。
     */
    List<Honor> listHonors();

    /**
     * 保存荣誉。
     *
     * @param honor 荣誉对象。
     * @return 保存后的荣誉。
     */
    Honor saveHonor(Honor honor);

    /**
     * 删除荣誉。
     *
     * @param id 荣誉主键。
     */
    void deleteHonor(Long id);

    /**
     * 查询全部照片。
     *
     * @return 照片列表。
     */
    List<Photo> listPhotos();

    /**
     * 保存照片。
     *
     * @param photo 照片对象。
     * @return 保存后的照片。
     */
    Photo savePhoto(Photo photo);

    /**
     * 删除照片。
     *
     * @param id 照片主键。
     */
    void deletePhoto(Long id);

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    Profile getProfile();

    /**
     * 保存个人资料。
     *
     * @param profile 个人资料对象。
     * @return 保存后的个人资料。
     */
    Profile saveProfile(Profile profile);

    /**
     * 查询后台用户列表。
     *
     * @return 用户列表。
     */
    List<User> listUsers();
}
