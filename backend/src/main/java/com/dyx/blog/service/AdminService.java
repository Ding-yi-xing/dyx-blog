package com.dyx.blog.service;

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

import java.time.LocalDateTime;
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
     * 获取访问日志列表（分页）。
     *
     * @param startTime 开始时间。
     * @param endTime 结束时间。
     * @param pageKey 页面标识。
     * @param deviceType 设备类型。
     * @param ipAddress IP 地址。
     * @param page 页码（从 1 开始）。
     * @param pageSize 每页数量。
     * @return 包含访问日志记录及分页信息的结果。
     */
    Map<String, Object> listVisitLogs(LocalDateTime startTime, LocalDateTime endTime, String pageKey, String deviceType, String ipAddress, Integer page, Integer pageSize);

    /**
     * 删除访问日志。
     *
     * @param id 访问日志主键。
     */
    void deleteVisitLog(Long id);

    /**
     * 批量删除访问日志。
     *
     * @param ids 访问日志主键列表。
     */
    void deleteVisitLogs(List<Long> ids);

    /**
     * 获取留言管理数据。
     *
     * @return 留言管理数据。
     */
    Map<String, Object> getGuestbookAdminData();

    /**
     * 更新留言页介绍。
     *
     * @param guestbookIntro 介绍文案。
     * @return 保存后的个人资料。
     */
    Profile saveGuestbookIntro(String guestbookIntro);

    /**
     * 更新留言。
     *
     * @param id 留言主键。
     * @param message 留言内容。
     * @return 保存后的留言。
     */
    GuestbookMessage updateGuestbookMessage(Long id, GuestbookMessage message);

    /**
     * 删除留言。
     *
     * @param id 留言主键。
     */
    void deleteGuestbookMessage(Long id);

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
     * 查询全部个人作品。
     *
     * @return 作品列表。
     */
    List<Work> listWorks();

    /**
     * 保存个人作品。
     *
     * @param work 作品对象。
     * @return 保存后的作品。
     */
    Work saveWork(Work work);

    /**
     * 删除个人作品。
     *
     * @param id 作品主键。
     */
    void deleteWork(Long id);

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
     * 查询全部首页足迹。
     *
     * @return 足迹列表。
     */
    List<Footprint> listFootprints();

    /**
     * 保存首页足迹。
     *
     * @param footprint 足迹对象。
     * @return 保存后的足迹。
     */
    Footprint saveFootprint(Footprint footprint);

    /**
     * 删除首页足迹。
     *
     * @param id 足迹主键。
     */
    void deleteFootprint(Long id);

    /**
     * 获取首页横幅配置。
     *
     * @return 首页横幅相关资料。
     */
    Profile getHeroProfile();

    /**
     * 保存首页横幅配置。
     *
     * @param profile 首页横幅相关资料。
     * @return 保存后的个人资料。
     */
    Profile saveHeroProfile(Profile profile);

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
     * 获取系统配置。
     *
     * @return 系统配置对象。
     */
    SystemConfig getSystemConfig();

    /**
     * 保存系统配置。
     *
     * @param systemConfig 系统配置对象。
     * @return 保存后的系统配置。
     */
    SystemConfig saveSystemConfig(SystemConfig systemConfig);

    /**
     * 查询后台用户列表。
     *
     * @return 用户列表。
     */
    List<User> listUsers();

    /**
     * 保存后台用户。
     *
     * @param user 用户对象。
     * @return 保存后的用户。
     */
    User saveUser(User user);

    /**
     * 删除后台用户。
     *
     * @param id 用户主键。
     */
    void deleteUser(Long id);
}
