package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Photo;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PhotoMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台站点服务实现类。
 */
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final PostMapper dyxPostMapper;
    private final MomentMapper dyxMomentMapper;
    private final ProjectMapper dyxProjectMapper;
    private final PhotoMapper dyxPhotoMapper;
    private final ProfileMapper dyxProfileMapper;

    /**
     * 获取首页聚合数据。
     *
     * @return 首页数据。
     */
    @Override
    public Map<String, Object> getHomeData() {
        Map<String, Object> result = new HashMap<>();
        result.put("profile", getProfile());
        result.put("latestPosts", listPosts().stream().limit(3).toList());
        result.put("latestMoments", listMoments().stream().limit(3).toList());
        result.put("featuredProjects", listProjects().stream().limit(3).toList());
        return result;
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    @Override
    public Profile getProfile() {
        return dyxProfileMapper.selectById(1L);
    }

    /**
     * 获取已发布文章列表。
     *
     * @return 文章列表。
     */
    @Override
    public List<Post> listPosts() {
        return dyxPostMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getPublished, 1)
                .orderByDesc(Post::getUpdatedAt));
    }

    /**
     * 获取文章详情。
     *
     * @param id 文章主键。
     * @return 文章详情对象。
     */
    @Override
    public Post getPostDetail(Long id) {
        return dyxPostMapper.selectById(id);
    }

    /**
     * 获取已发布动态列表。
     *
     * @return 动态列表。
     */
    @Override
    public List<Moment> listMoments() {
        return dyxMomentMapper.selectList(new LambdaQueryWrapper<Moment>()
                .eq(Moment::getPublished, 1)
                .orderByDesc(Moment::getHappenedAt));
    }

    /**
     * 获取已发布项目经历列表。
     *
     * @return 项目经历列表。
     */
    @Override
    public List<Project> listProjects() {
        return dyxProjectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getPublished, 1)
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getUpdatedAt));
    }

    /**
     * 获取已发布照片列表。
     *
     * @return 照片列表。
     */
    @Override
    public List<Photo> listPhotos() {
        return dyxPhotoMapper.selectList(new LambdaQueryWrapper<Photo>()
                .eq(Photo::getPublished, 1)
                .orderByAsc(Photo::getSortOrder)
                .orderByDesc(Photo::getUpdatedAt));
    }
}
