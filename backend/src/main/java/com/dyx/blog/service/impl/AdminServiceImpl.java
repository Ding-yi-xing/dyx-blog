package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.constant.SystemConstant;
import com.dyx.blog.common.context.UserContext;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.HeroConfigUtil;
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
import com.dyx.blog.mapper.FootprintMapper;
import com.dyx.blog.mapper.GuestbookMessageMapper;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.SiteVisitLogMapper;
import com.dyx.blog.mapper.SystemConfigMapper;
import com.dyx.blog.mapper.UserMapper;
import com.dyx.blog.mapper.WorkMapper;
import com.dyx.blog.service.AdminService;
import com.dyx.blog.storage.OssMediaStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理服务实现类。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final int RECENT_VISIT_LIMIT = 8;

    private final PostMapper dyxPostMapper;
    private final MomentMapper dyxMomentMapper;
    private final ProjectMapper dyxProjectMapper;
    private final WorkMapper dyxWorkMapper;
    private final ProfileMapper dyxProfileMapper;
    private final UserMapper dyxUserMapper;
    private final HonorMapper dyxHonorMapper;
    private final FootprintMapper dyxFootprintMapper;
    private final GuestbookMessageMapper dyxGuestbookMessageMapper;
    private final SiteVisitLogMapper dyxSiteVisitLogMapper;
    private final SystemConfigMapper dyxSystemConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取后台仪表盘摘要。
     *
     * @return 摘要数据。
     */
    @Override
    public Map<String, Object> getDashboardSummary() {
        ensureVisitStatTable();
        ensureVisitLogTable();

        Map<String, Object> result = new HashMap<>();
        result.put("postCount", dyxPostMapper.selectCount(null));
        result.put("momentCount", dyxMomentMapper.selectCount(null));
        result.put("honorCount", dyxHonorMapper.selectCount(null));
        result.put("userCount", dyxUserMapper.selectCount(null));
        result.put("totalPostViews", queryTotalPostViews());
        result.put("totalSiteVisits", queryTotalSiteVisits());
        result.put("dailySiteVisits", queryDailySiteVisits());
        result.put("deviceTypeDistribution", queryDeviceTypeDistribution());
        result.put("topVisitedPages", queryTopVisitedPages());
        return result;
    }

    /**
     * 获取访问日志列表（分页）。
     *
     * @return 包含访问日志记录及分页信息的结果。
     */
    @Override
    public Map<String, Object> listVisitLogs(LocalDateTime startTime, LocalDateTime endTime, String pageKey, String deviceType, String ipAddress, Integer page, Integer pageSize) {
        ensureVisitLogTable();
        int pageNo = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize <= 0) ? 20 : pageSize;
        return queryVisitLogs(startTime, endTime, pageKey, deviceType, ipAddress, pageNo, size);
    }

    /**
     * 删除访问日志。
     *
     * @param id 访问日志主键。
     */
    @Override
    public void deleteVisitLog(Long id) {
        ensureVisitLogTable();
        dyxSiteVisitLogMapper.deleteById(id);
    }

    /**
     * 批量删除访问日志。
     *
     * @param ids 访问日志主键列表。
     */
    @Override
    @Transactional
    public void deleteVisitLogs(List<Long> ids) {
        ensureVisitLogTable();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择需要删除的访问日志");
        }
        dyxSiteVisitLogMapper.deleteBatchIds(ids);
    }

    /**
     * 获取留言管理数据。
     *
     * @return 留言管理数据。
     */
    @Override
    public Map<String, Object> getGuestbookAdminData() {
        ensureGuestbookMessageTable();
        Profile profile = getProfile();
        Map<String, Object> result = new HashMap<>();
        result.put("guestbookIntro", profile.getGuestbookIntro());
        result.put("messages", dyxGuestbookMessageMapper.selectList(new LambdaQueryWrapper<GuestbookMessage>()
                .orderByDesc(GuestbookMessage::getCreatedAt)
                .orderByDesc(GuestbookMessage::getId)));
        return result;
    }

    /**
     * 更新留言页介绍。
     *
     * @param guestbookIntro 介绍文案。
     * @return 保存后的个人资料。
     */
    @Override
    public Profile saveGuestbookIntro(String guestbookIntro) {
        Profile profile = getProfile();
        profile.setGuestbookIntro(guestbookIntro == null ? null : guestbookIntro.trim());
        return saveProfile(profile);
    }

    /**
     * 更新留言。
     *
     * @param id 留言主键。
     * @param message 留言内容。
     * @return 保存后的留言。
     */
    @Override
    public GuestbookMessage updateGuestbookMessage(Long id, GuestbookMessage message) {
        ensureGuestbookMessageTable();
        GuestbookMessage existingMessage = dyxGuestbookMessageMapper.selectById(id);
        if (existingMessage == null) {
            throw new BusinessException("留言不存在");
        }
        if (message != null && message.getContent() != null) {
            String content = message.getContent().trim();
            if (content.isEmpty()) {
                throw new BusinessException("留言内容不能为空");
            }
            if (content.length() > 2000) {
                throw new BusinessException("留言内容不能超过 2000 个字符");
            }
            existingMessage.setContent(content);
        }
        if (message != null && message.getPublished() != null) {
            existingMessage.setPublished(message.getPublished() == 1 ? 1 : 0);
        }
        existingMessage.setUpdatedAt(LocalDateTime.now());
        dyxGuestbookMessageMapper.updateById(existingMessage);
        return existingMessage;
    }

    /**
     * 删除留言。
     *
     * @param id 留言主键。
     */
    @Override
    public void deleteGuestbookMessage(Long id) {
        ensureGuestbookMessageTable();
        dyxGuestbookMessageMapper.deleteById(id);
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
        ensureMomentImageUrlsColumn();
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
        ensureMomentImageUrlsColumn();
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
     * 查询全部个人作品。
     *
     * @return 作品列表。
     */
    @Override
    public List<Work> listWorks() {
        return dyxWorkMapper.selectList(new LambdaQueryWrapper<Work>()
                .orderByAsc(Work::getSortOrder)
                .orderByDesc(Work::getUpdatedAt));
    }

    /**
     * 保存个人作品。
     *
     * @param work 作品对象。
     * @return 保存后的作品。
     */
    @Override
    public Work saveWork(Work work) {
        LocalDateTime now = LocalDateTime.now();
        work.setUpdatedAt(now);
        if (work.getId() == null) {
            work.setCreatedAt(now);
            dyxWorkMapper.insert(work);
        } else {
            dyxWorkMapper.updateById(work);
        }
        return work;
    }

    /**
     * 删除个人作品。
     *
     * @param id 作品主键。
     */
    @Override
    public void deleteWork(Long id) {
        dyxWorkMapper.deleteById(id);
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
     * 查询全部首页足迹。
     *
     * @return 足迹列表。
     */
    @Override
    public List<Footprint> listFootprints() {
        ensureFootprintTable();
        return dyxFootprintMapper.selectList(new LambdaQueryWrapper<Footprint>()
                .orderByDesc(Footprint::getImportance)
                .orderByAsc(Footprint::getSortOrder)
                .orderByDesc(Footprint::getVisitedAt)
                .orderByDesc(Footprint::getUpdatedAt));
    }

    /**
     * 保存首页足迹。
     *
     * @param footprint 足迹对象。
     * @return 保存后的足迹。
     */
    @Override
    public Footprint saveFootprint(Footprint footprint) {
        ensureFootprintTable();
        validateFootprint(footprint);
        LocalDateTime now = LocalDateTime.now();
        footprint.setUpdatedAt(now);
        if (footprint.getId() == null) {
            footprint.setCreatedAt(now);
            dyxFootprintMapper.insert(footprint);
        } else {
            Footprint existingFootprint = dyxFootprintMapper.selectById(footprint.getId());
            if (existingFootprint == null) {
                throw new BusinessException("足迹记录不存在");
            }
            if (footprint.getCreatedAt() == null) {
                footprint.setCreatedAt(existingFootprint.getCreatedAt());
            }
            dyxFootprintMapper.updateById(footprint);
        }
        return footprint;
    }

    /**
     * 删除首页足迹。
     *
     * @param id 足迹主键。
     */
    @Override
    public void deleteFootprint(Long id) {
        ensureFootprintTable();
        dyxFootprintMapper.deleteById(id);
    }

    /**
     * 获取首页横幅配置。
     *
     * @return 首页横幅相关资料。
     */
    @Override
    public Profile getHeroProfile() {
        return getProfile();
    }

    /**
     * 保存首页横幅配置。
     *
     * @param profile 首页横幅相关资料。
     * @return 保存后的个人资料。
     */
    @Override
    public Profile saveHeroProfile(Profile profile) {
        Profile existingProfile = getProfile();
        existingProfile.setHeroConfig(profile.getHeroConfig());
        existingProfile.setSiteTitle(profile.getSiteTitle());
        existingProfile.setHeroTitle(profile.getHeroTitle());
        existingProfile.setHeroSubtitle(profile.getHeroSubtitle());
        return saveProfile(existingProfile);
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    @Override
    public Profile getProfile() {
        ensureProfileContactMethodsColumn();
        ensureProfileGuestbookIntroColumn();
        Profile profile = dyxProfileMapper.selectById(1L);
        if (profile == null) {
            profile = new Profile();
            profile.setId(1L);
        }
        HeroConfigUtil.ensureHeroConfig(profile, objectMapper);
        HeroConfigUtil.syncLegacyFields(profile, objectMapper);
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
        ensureProfileContactMethodsColumn();
        ensureProfileGuestbookIntroColumn();
        HeroConfigUtil.ensureHeroConfig(profile, objectMapper);
        HeroConfigUtil.syncLegacyFields(profile, objectMapper);
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
     * 获取系统配置。
     *
     * @return 系统配置对象。
     */
    @Override
    public SystemConfig getSystemConfig() {
        ensureSystemConfigTable();
        SystemConfig systemConfig = dyxSystemConfigMapper.selectById(1L);
        if (systemConfig == null) {
            systemConfig = new SystemConfig();
            systemConfig.setId(1L);
            systemConfig.setStorageType("local");
            systemConfig.setUpdatedAt(LocalDateTime.now());
        }
        return systemConfig;
    }

    /**
     * 保存系统配置。
     *
     * @param systemConfig 系统配置对象。
     * @return 保存后的系统配置。
     */
    @Override
    public SystemConfig saveSystemConfig(SystemConfig systemConfig) {
        ensureSystemConfigTable();
        if (systemConfig == null) {
            throw new BusinessException("系统配置不能为空");
        }
        SystemConfig existingConfig = dyxSystemConfigMapper.selectById(1L);
        SystemConfig targetConfig = existingConfig == null ? new SystemConfig() : existingConfig;
        targetConfig.setId(1L);
        targetConfig.setStorageType(normalizeStorageType(systemConfig.getStorageType()));
        targetConfig.setOssEndpoint(normalizeNullableValue(systemConfig.getOssEndpoint()));
        targetConfig.setOssRegion(normalizeNullableValue(systemConfig.getOssRegion()));
        targetConfig.setOssBucketName(normalizeNullableValue(systemConfig.getOssBucketName()));
        targetConfig.setOssPublicUrlPrefix(normalizeNullableValue(systemConfig.getOssPublicUrlPrefix()));
        targetConfig.setOssBaseDir(normalizeNullableValue(systemConfig.getOssBaseDir()));
        targetConfig.setFootprintEyebrow(normalizeNullableValue(systemConfig.getFootprintEyebrow()));
        targetConfig.setFootprintTitle(normalizeNullableValue(systemConfig.getFootprintTitle()));
        targetConfig.setFootprintSubtitle(normalizeNullableValue(systemConfig.getFootprintSubtitle()));
        targetConfig.setFootprintDescription(normalizeNullableValue(systemConfig.getFootprintDescription()));
        targetConfig.setCopyrightText(normalizeNullableValue(systemConfig.getCopyrightText()));
        targetConfig.setTechSupportText(normalizeNullableValue(systemConfig.getTechSupportText()));
        validateSystemConfig(targetConfig);
        targetConfig.setUpdatedAt(LocalDateTime.now());
        if (existingConfig == null) {
            dyxSystemConfigMapper.insert(targetConfig);
        } else {
            dyxSystemConfigMapper.updateById(targetConfig);
        }
        return targetConfig;
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

    /**
     * 保存后台用户。
     *
     * @param user 用户对象。
     * @return 保存后的用户。
     */
    @Override
    public User saveUser(User user) {
        validateUser(user);
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);
        if (user.getId() == null) {
            user.setCreatedAt(now);
            dyxUserMapper.insert(user);
        } else {
            User existingUser = dyxUserMapper.selectById(user.getId());
            if (existingUser == null) {
                throw new BusinessException("用户不存在");
            }
            if (isBlank(user.getPassword())) {
                user.setPassword(existingUser.getPassword());
            }
            if (existingUser.getId().equals(UserContext.getUserId()) && !SystemConstant.ROLE_ADMIN.equals(user.getRole())) {
                throw new BusinessException("当前登录账号必须保留管理员权限");
            }
            dyxUserMapper.updateById(user);
        }
        return user;
    }

    /**
     * 删除后台用户。
     *
     * @param id 用户主键。
     */
    @Override
    public void deleteUser(Long id) {
        User targetUser = dyxUserMapper.selectById(id);
        if (targetUser == null) {
            return;
        }
        if (targetUser.getId().equals(UserContext.getUserId())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        Long adminCount = dyxUserMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, SystemConstant.ROLE_ADMIN));
        if (SystemConstant.ROLE_ADMIN.equals(targetUser.getRole()) && adminCount != null && adminCount <= 1) {
            throw new BusinessException("至少保留一个管理员账号");
        }
        dyxUserMapper.deleteById(id);
    }

    private long queryTotalPostViews() {
        Long total = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(view_count), 0) FROM dyx_post", Long.class);
        return total == null ? 0L : total;
    }

    private long queryTotalSiteVisits() {
        Long total = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(visit_count), 0) FROM dyx_site_visit_stat", Long.class);
        return total == null ? 0L : total;
    }

    private List<Map<String, Object>> queryDailySiteVisits() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS visit_day, COUNT(*) AS visit_count "
                        + "FROM dyx_site_visit_log WHERE created_at >= ? GROUP BY DATE(created_at) ORDER BY visit_day ASC",
                startDate.atStartOfDay()
        );
        Map<String, Long> visitCountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object visitDay = row.get("visit_day");
            Object visitCount = row.get("visit_count");
            if (visitDay == null) {
                continue;
            }
            visitCountMap.put(String.valueOf(visitDay), visitCount == null ? 0L : ((Number) visitCount).longValue());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = startDate.plusDays(index);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("label", date.format(DAY_LABEL_FORMATTER));
            item.put("visitCount", visitCountMap.getOrDefault(date.toString(), 0L));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> queryDeviceTypeDistribution() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT device_type, COUNT(*) AS visit_count FROM dyx_site_visit_log GROUP BY device_type ORDER BY visit_count DESC, device_type ASC"
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            String deviceType = String.valueOf(row.getOrDefault("device_type", "UNKNOWN"));
            item.put("deviceType", deviceType);
            item.put("label", mapDeviceTypeLabel(deviceType));
            item.put("visitCount", row.get("visit_count") == null ? 0L : ((Number) row.get("visit_count")).longValue());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> queryTopVisitedPages() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT page_key, COUNT(*) AS visit_count, MAX(created_at) AS last_visit_at "
                        + "FROM dyx_site_visit_log GROUP BY page_key ORDER BY visit_count DESC, last_visit_at DESC LIMIT 6"
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            String pageKey = String.valueOf(row.getOrDefault("page_key", "unknown"));
            item.put("pageKey", pageKey);
            item.put("label", mapPageKeyLabel(pageKey));
            item.put("visitCount", row.get("visit_count") == null ? 0L : ((Number) row.get("visit_count")).longValue());
            item.put("lastVisitAt", row.get("last_visit_at"));
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> queryVisitLogs(LocalDateTime startTime, LocalDateTime endTime, String pageKey, String deviceType, String ipAddress, int page, int pageSize) {
        StringBuilder baseSql = new StringBuilder(" FROM dyx_site_visit_log WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (startTime != null) {
            baseSql.append(" AND created_at >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            baseSql.append(" AND created_at <= ?");
            params.add(endTime);
        }
        if (!isBlank(pageKey)) {
            baseSql.append(" AND page_key = ?");
            params.add(pageKey.trim());
        }
        if (!isBlank(deviceType)) {
            baseSql.append(" AND device_type = ?");
            params.add(deviceType.trim());
        }
        if (!isBlank(ipAddress)) {
            baseSql.append(" AND ip_address = ?");
            params.add(ipAddress.trim());
        }

        String countSql = "SELECT COUNT(*)" + baseSql;
        Long total = jdbcTemplate.queryForObject(countSql, params.toArray(), Long.class);
        long totalCount = (total == null) ? 0L : total;

        List<Map<String, Object>> records = new ArrayList<>();
        if (totalCount > 0) {
            StringBuilder dataSql = new StringBuilder(
                    "SELECT id, page_key, ip_address, user_agent, device_type, device_name, created_at");
            dataSql.append(baseSql).append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            List<Object> dataParams = new ArrayList<>(params);
            dataParams.add(pageSize);
            dataParams.add((long) (page - 1) * pageSize);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql.toString(), dataParams.toArray());
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                String currentPageKey = String.valueOf(row.getOrDefault("page_key", "unknown"));
                String currentDeviceType = String.valueOf(row.getOrDefault("device_type", "UNKNOWN"));
                item.put("id", row.get("id"));
                item.put("pageKey", currentPageKey);
                item.put("pageLabel", mapPageKeyLabel(currentPageKey));
                item.put("ipAddress", row.getOrDefault("ip_address", "unknown"));
                item.put("userAgent", row.getOrDefault("user_agent", "unknown"));
                item.put("deviceType", currentDeviceType);
                item.put("deviceTypeLabel", mapDeviceTypeLabel(currentDeviceType));
                item.put("deviceName", row.getOrDefault("device_name", "Unknown"));
                item.put("createdAt", row.get("created_at"));
                records.add(item);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    private String mapPageKeyLabel(String pageKey) {
        return switch (pageKey) {
            case "home" -> "首页";
            case "profile" -> "关于我";
            case "resume" -> "简历";
            case "moments" -> "动态";
            case "moment-detail" -> "动态详情";
            case "blog" -> "博客";
            case "blog-detail" -> "博客详情";
            case "guestbook" -> "留言";
            default -> pageKey;
        };
    }

    private String mapDeviceTypeLabel(String deviceType) {
        return switch (deviceType) {
            case "DESKTOP" -> "桌面端";
            case "MOBILE" -> "手机端";
            case "TABLET" -> "平板端";
            case "BOT" -> "爬虫 / Bot";
            default -> "未知设备";
        };
    }

    private void ensureFootprintTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_footprint ("
                + "id BIGINT PRIMARY KEY, "
                + "city_name VARCHAR(100) NOT NULL, "
                + "country_name VARCHAR(100), "
                + "region_name VARCHAR(100), "
                + "position_x DECIMAL(5,2) NOT NULL DEFAULT 0, "
                + "position_y DECIMAL(5,2) NOT NULL DEFAULT 0, "
                + "visited_at DATETIME, "
                + "description TEXT, "
                + "importance INT NOT NULL DEFAULT 1, "
                + "sort_order INT NOT NULL DEFAULT 0, "
                + "published TINYINT NOT NULL DEFAULT 0, "
                + "created_at DATETIME NOT NULL, "
                + "updated_at DATETIME NOT NULL, "
                + "INDEX idx_footprint_published (published), "
                + "INDEX idx_footprint_sort_order (sort_order), "
                + "INDEX idx_footprint_visited_at (visited_at))");
    }

    private void ensureVisitStatTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_site_visit_stat ("
                + "page_key VARCHAR(64) PRIMARY KEY, "
                + "visit_count BIGINT NOT NULL DEFAULT 0, "
                + "updated_at DATETIME NOT NULL)");
    }

    private void ensureMomentImageUrlsColumn() {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_moment'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_moment' AND COLUMN_NAME = 'image_urls'",
                Integer.class
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE dyx_moment ADD COLUMN image_urls TEXT NULL AFTER cover_image");
        }
    }

    private void ensureProfileContactMethodsColumn() {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile' AND COLUMN_NAME = 'contact_methods'",
                Integer.class
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE dyx_profile ADD COLUMN contact_methods LONGTEXT NULL AFTER github_url");
        }
    }

    private void ensureProfileGuestbookIntroColumn() {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_profile' AND COLUMN_NAME = 'guestbook_intro'",
                Integer.class
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE dyx_profile ADD COLUMN guestbook_intro TEXT NULL AFTER resume_pdf_url");
        }
    }

    private void ensureGuestbookMessageTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_guestbook_message ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                + "content TEXT NOT NULL, "
                + "published TINYINT NOT NULL DEFAULT 0, "
                + "ip_address VARCHAR(45) NOT NULL, "
                + "created_at DATETIME NOT NULL, "
                + "updated_at DATETIME NOT NULL, "
                + "INDEX idx_guestbook_message_published (published), "
                + "INDEX idx_guestbook_message_created_at (created_at))");
    }

    private void ensureVisitLogTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_site_visit_log ("
                + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
                + "page_key VARCHAR(64) NOT NULL, "
                + "ip_address VARCHAR(45) NOT NULL, "
                + "user_agent VARCHAR(512), "
                + "device_type VARCHAR(32) NOT NULL, "
                + "device_name VARCHAR(128), "
                + "created_at DATETIME NOT NULL, "
                + "INDEX idx_site_visit_log_created_at (created_at), "
                + "INDEX idx_site_visit_log_page_key (page_key), "
                + "INDEX idx_site_visit_log_device_type (device_type))");
        ensureSiteVisitLogDeviceNameColumn();
    }

    private void ensureSiteVisitLogDeviceNameColumn() {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_site_visit_log'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_site_visit_log' AND COLUMN_NAME = 'device_name'",
                Integer.class
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE dyx_site_visit_log ADD COLUMN device_name VARCHAR(128) AFTER device_type");
        }
    }

    private void ensureSystemConfigTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS dyx_system_config ("
                + "id BIGINT PRIMARY KEY, "
                + "storage_type VARCHAR(32) NOT NULL DEFAULT 'local', "
                + "oss_endpoint VARCHAR(255), "
                + "oss_region VARCHAR(100), "
                + "oss_bucket_name VARCHAR(255), "
                + "oss_public_url_prefix VARCHAR(255), "
                + "oss_base_dir VARCHAR(255), "
                + "footprint_eyebrow VARCHAR(120), "
                + "footprint_title VARCHAR(200), "
                + "footprint_subtitle VARCHAR(255), "
                + "footprint_description VARCHAR(500), "
                + "copyright_text VARCHAR(255), "
                + "tech_support_text VARCHAR(255), "
                + "updated_at DATETIME NOT NULL)");
        ensureSystemConfigColumn("footprint_eyebrow", "ALTER TABLE dyx_system_config ADD COLUMN footprint_eyebrow VARCHAR(120) NULL AFTER oss_base_dir");
        ensureSystemConfigColumn("footprint_title", "ALTER TABLE dyx_system_config ADD COLUMN footprint_title VARCHAR(200) NULL AFTER footprint_eyebrow");
        ensureSystemConfigColumn("footprint_subtitle", "ALTER TABLE dyx_system_config ADD COLUMN footprint_subtitle VARCHAR(255) NULL AFTER footprint_title");
        ensureSystemConfigColumn("footprint_description", "ALTER TABLE dyx_system_config ADD COLUMN footprint_description VARCHAR(500) NULL AFTER footprint_subtitle");
        ensureSystemConfigColumn("copyright_text", "ALTER TABLE dyx_system_config ADD COLUMN copyright_text VARCHAR(255) NULL AFTER footprint_description");
        ensureSystemConfigColumn("tech_support_text", "ALTER TABLE dyx_system_config ADD COLUMN tech_support_text VARCHAR(255) NULL AFTER copyright_text");
    }

    private void ensureSystemConfigColumn(String columnName, String alterSql) {
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dyx_system_config' AND COLUMN_NAME = ?",
                Integer.class,
                columnName
        );
        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void validateSystemConfig(SystemConfig systemConfig) {
        if (!"local".equals(systemConfig.getStorageType()) && !"oss".equals(systemConfig.getStorageType())) {
            throw new BusinessException("存储方式仅支持 local 或 oss");
        }
        if (!"oss".equals(systemConfig.getStorageType())) {
            return;
        }
        if (isBlank(systemConfig.getOssEndpoint())) {
            throw new BusinessException("请先配置 OSS Endpoint");
        }
        if (isBlank(systemConfig.getOssBucketName())) {
            throw new BusinessException("请先配置 OSS Bucket");
        }
        if (!OssMediaStorage.hasConfiguredCredentials()) {
            throw new BusinessException("OSS 凭证未配置，请先在服务端环境变量中设置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET");
        }
    }

    private String normalizeStorageType(String storageType) {
        return isBlank(storageType) ? "local" : storageType.trim().toLowerCase();
    }

    private String normalizeNullableValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateFootprint(Footprint footprint) {
        if (footprint == null) {
            throw new BusinessException("足迹数据不能为空");
        }
        if (isBlank(footprint.getCityName())) {
            throw new BusinessException("城市名称不能为空");
        }
        if (footprint.getPositionX() == null || footprint.getPositionX() < 0 || footprint.getPositionX() > 100) {
            throw new BusinessException("横向坐标需在 0 到 100 之间");
        }
        if (footprint.getPositionY() == null || footprint.getPositionY() < 0 || footprint.getPositionY() > 100) {
            throw new BusinessException("纵向坐标需在 0 到 100 之间");
        }
        if (footprint.getImportance() == null) {
            footprint.setImportance(1);
        }
        if (footprint.getImportance() < 1 || footprint.getImportance() > 5) {
            throw new BusinessException("高亮等级需在 1 到 5 之间");
        }
        if (footprint.getSortOrder() == null) {
            footprint.setSortOrder(0);
        }
        if (footprint.getPublished() == null) {
            footprint.setPublished(0);
        }
        if (footprint.getPublished() != 0 && footprint.getPublished() != 1) {
            throw new BusinessException("发布状态仅支持 0 或 1");
        }
        footprint.setCityName(footprint.getCityName().trim());
        footprint.setCountryName(normalizeNullableValue(footprint.getCountryName()));
        footprint.setRegionName(normalizeNullableValue(footprint.getRegionName()));
        footprint.setDescription(normalizeNullableValue(footprint.getDescription()));
    }

    private void validateUser(User user) {
        if (isBlank(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (isBlank(user.getDisplayName())) {
            throw new BusinessException("显示名称不能为空");
        }
        if (isBlank(user.getRole())) {
            throw new BusinessException("角色不能为空");
        }
        if (user.getEnabled() == null) {
            throw new BusinessException("启用状态不能为空");
        }
        if (user.getId() == null && isBlank(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        User duplicatedUser = dyxUserMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername())
                .ne(user.getId() != null, User::getId, user.getId())
                .last("LIMIT 1"));
        if (duplicatedUser != null) {
            throw new BusinessException("用户名已存在");
        }
        if (!SystemConstant.ROLE_ADMIN.equals(user.getRole()) && user.getId() != null && user.getId().equals(UserContext.getUserId())) {
            throw new BusinessException("当前登录账号必须保留管理员权限");
        }
        if (user.getId() != null && user.getId().equals(UserContext.getUserId()) && user.getEnabled() != 1) {
            throw new BusinessException("不能停用当前登录账号");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
