package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.User;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PhotoMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.UserMapper;
import com.dyx.blog.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理服务实现类。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final PostMapper dyxPostMapper;
    private final MomentMapper dyxMomentMapper;
    private final ProjectMapper dyxProjectMapper;
    private final PhotoMapper dyxPhotoMapper;
    private final ProfileMapper dyxProfileMapper;
    private final UserMapper dyxUserMapper;
    private final HonorMapper dyxHonorMapper;

    /**
     * 获取后台仪表盘摘要。
     *
     * @return 摘要数据。
     */
    @Override
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("postCount", dyxPostMapper.selectCount(null));
        result.put("momentCount", dyxMomentMapper.selectCount(null));
        result.put("honorCount", dyxHonorMapper.selectCount(null));
        result.put("photoCount", dyxPhotoMapper.selectCount(null));
        result.put("userCount", dyxUserMapper.selectCount(null));
        return result;
    }

    /**
     * 查询全部文章。
     *
     * @return 文章列表。
     */
    @Override
    public List<Post> listPosts() {
        return dyxPostMapper.selectList(new LambdaQueryWrapper<Post>().orderByDesc(Post::getUpdatedAt));
    }

    /**
     * 保存文章。
     *
     * @param post 文章对象。
     * @return 保存后的文章。
     */
    @Override
    public Post savePost(Post post) {
        LocalDateTime now = LocalDateTime.now();
        post.setUpdatedAt(now);
        if (post.getId() == null) {
            post.setCreatedAt(now);
            dyxPostMapper.insert(post);
        } else {
            dyxPostMapper.updateById(post);
        }
        return post;
    }

    /**
     * 删除文章。
     *
     * @param id 文章主键。
     */
    @Override
    public void deletePost(Long id) {
        dyxPostMapper.deleteById(id);
    }

    /**
     * 查询全部动态。
     *
     * @return 动态列表。
     */
    @Override
    public List<Moment> listMoments() {
        return dyxMomentMapper.selectList(new LambdaQueryWrapper<Moment>().orderByDesc(Moment::getUpdatedAt));
    }

    /**
     * 保存动态。
     *
     * @param moment 动态对象。
     * @return 保存后的动态。
     */
    @Override
    public Moment saveMoment(Moment moment) {
        LocalDateTime now = LocalDateTime.now();
        moment.setUpdatedAt(now);
        if (moment.getId() == null) {
            moment.setCreatedAt(now);
            dyxMomentMapper.insert(moment);
        } else {
            dyxMomentMapper.updateById(moment);
        }
        return moment;
    }

    /**
     * 删除动态。
     *
     * @param id 动态主键。
     */
    @Override
    public void deleteMoment(Long id) {
        dyxMomentMapper.deleteById(id);
    }

    /**
     * 查询全部项目经历。
     *
     * @return 项目经历列表。
     */
    @Override
    public List<Project> listProjects() {
        return dyxProjectMapper.selectList(new LambdaQueryWrapper<Project>()
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getUpdatedAt));
    }

    /**
     * 保存项目经历。
     *
     * @param project 项目经历对象。
     * @return 保存后的项目经历。
     */
    @Override
    public Project saveProject(Project project) {
        LocalDateTime now = LocalDateTime.now();
        project.setUpdatedAt(now);
        if (project.getId() == null) {
            project.setCreatedAt(now);
            dyxProjectMapper.insert(project);
        } else {
            dyxProjectMapper.updateById(project);
        }
        return project;
    }

    /**
     * 删除项目经历。
     *
     * @param id 项目经历主键。
     */
    @Override
    public void deleteProject(Long id) {
        dyxProjectMapper.deleteById(id);
    }

    /**
     * 查询全部荣誉。
     *
     * @return 荣誉列表。
     */
    @Override
    public List<Honor> listHonors() {
        return dyxHonorMapper.selectList(new LambdaQueryWrapper<Honor>()
                .orderByDesc(Honor::getAwardAt)
                .orderByAsc(Honor::getSortOrder)
                .orderByDesc(Honor::getUpdatedAt));
    }

    /**
     * 保存荣誉。
     *
     * @param honor 荣誉对象。
     * @return 保存后的荣誉。
     */
    @Override
    public Honor saveHonor(Honor honor) {
        LocalDateTime now = LocalDateTime.now();
        honor.setUpdatedAt(now);
        if (honor.getId() == null) {
            honor.setCreatedAt(now);
            dyxHonorMapper.insert(honor);
        } else {
            dyxHonorMapper.updateById(honor);
        }
        return honor;
    }

    /**
     * 删除荣誉。
     *
     * @param id 荣誉主键。
     */
    @Override
    public void deleteHonor(Long id) {
        dyxHonorMapper.deleteById(id);
    }

    /**
     * 查询全部照片。
     *
     * @return 照片列表。
     */
    @Override
    public List<Photo> listPhotos() {
        return dyxPhotoMapper.selectList(new LambdaQueryWrapper<Photo>()
                .orderByAsc(Photo::getSortOrder)
                .orderByDesc(Photo::getUpdatedAt));
    }

    /**
     * 保存照片。
     *
     * @param photo 照片对象。
     * @return 保存后的照片。
     */
    @Override
    public Photo savePhoto(Photo photo) {
        LocalDateTime now = LocalDateTime.now();
        photo.setUpdatedAt(now);
        if (photo.getId() == null) {
            photo.setCreatedAt(now);
            dyxPhotoMapper.insert(photo);
        } else {
            dyxPhotoMapper.updateById(photo);
        }
        return photo;
    }

    /**
     * 删除照片。
     *
     * @param id 照片主键。
     */
    @Override
    public void deletePhoto(Long id) {
        dyxPhotoMapper.deleteById(id);
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    @Override
    public Profile getProfile() {
        Profile profile = dyxProfileMapper.selectById(1L);
        if (profile == null) {
            profile = new Profile();
            profile.setId(1L);
        }
        return profile;
    }

    /**
     * 保存个人资料。
     *
     * @param profile 个人资料对象。
     * @return 保存后的个人资料。
     */
    @Override
    public Profile saveProfile(Profile profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        if (profile.getId() == null) {
            profile.setId(1L);
        }
        if (dyxProfileMapper.selectById(profile.getId()) == null) {
            dyxProfileMapper.insert(profile);
        } else {
            dyxProfileMapper.updateById(profile);
        }
        return profile;
    }

    /**
     * 查询后台用户列表。
     *
     * @return 用户列表。
     */
    @Override
    public List<User> listUsers() {
        return dyxUserMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getUpdatedAt));
    }
}
