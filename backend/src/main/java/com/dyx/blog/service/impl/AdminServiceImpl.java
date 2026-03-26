package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.constant.SystemConstant;
import com.dyx.blog.common.context.UserContext;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.HeroConfigUtil;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.User;
import com.dyx.blog.entity.Work;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.SiteVisitLogMapper;
import com.dyx.blog.mapper.UserMapper;
import com.dyx.blog.mapper.WorkMapper;
import com.dyx.blog.service.AdminService;
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
    private final SiteVisitLogMapper dyxSiteVisitLogMapper;
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
