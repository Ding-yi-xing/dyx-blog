package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.dto.GuestbookDataDTO;
import com.dyx.blog.common.dto.HomeDataDTO;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.ClientIpUtil;
import com.dyx.blog.common.util.HeroConfigUtil;
import com.dyx.blog.common.util.XssUtil;
import com.dyx.blog.entity.Footprint;
import com.dyx.blog.entity.GuestbookMessage;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.SystemConfig;
import com.dyx.blog.entity.Work;
import com.dyx.blog.entity.SiteVisitLog;
import com.dyx.blog.mapper.FootprintMapper;
import com.dyx.blog.mapper.GuestbookMessageMapper;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.SiteVisitLogMapper;
import com.dyx.blog.mapper.SystemConfigMapper;
import com.dyx.blog.mapper.WorkMapper;
import com.dyx.blog.service.SiteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前台站点服务实现类。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {

    private static final Pattern ANDROID_DEVICE_PATTERN = Pattern.compile(";\\s*([^;\\)]+?)\\s+Build/",
            Pattern.CASE_INSENSITIVE);

    private final PostMapper dyxPostMapper;
    private final MomentMapper dyxMomentMapper;
    private final ProjectMapper dyxProjectMapper;
    private final WorkMapper dyxWorkMapper;
    private final ProfileMapper dyxProfileMapper;
    private final SystemConfigMapper dyxSystemConfigMapper;
    private final HonorMapper dyxHonorMapper;
    private final FootprintMapper dyxFootprintMapper;
    private final GuestbookMessageMapper dyxGuestbookMessageMapper;
    private final SiteVisitLogMapper dyxSiteVisitLogMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取首页聚合数据。
     *
     * @return 首页数据。
     */
    @Override
    public HomeDataDTO getHomeData() {
        return HomeDataDTO.builder()
                .profile(getProfile())
                .latestPosts(listPosts(1, 3))
                .latestMoments(listMoments().stream().limit(3).toList())
                .featuredProjects(listProjects().stream().limit(3).toList())
                .latestHonors(listHonors().stream().limit(3).toList())
                .footprints(listFootprints())
                .systemConfig(getHomeSystemConfig())
                .build();
    }

    /**
     * 获取个人资料。
     *
     * @return 个人资料对象。
     */
    @Override
    @Cacheable(value = "site", key = "'profile'")
    public Profile getProfile() {
        Profile profile = dyxProfileMapper.selectById(1L);
        if (profile == null) {
            profile = new Profile();
            profile.setId(1L);
        }
        HeroConfigUtil.ensureHeroConfig(profile, objectMapper);
        HeroConfigUtil.syncLegacyFields(profile, objectMapper);
        profile.setResumePdfUrl(resolvePublicResumePdfUrl(profile.getResumePdfUrl()));
        return profile;
    }

    /**
     * 获取留言页数据。
     *
     * @return 留言页数据。
     */
    @Override
    public GuestbookDataDTO getGuestbookData() {
        Profile profile = getProfile();
        return GuestbookDataDTO.builder()
                .guestbookIntro(profile.getGuestbookIntro())
                .messages(dyxGuestbookMessageMapper.selectList(new LambdaQueryWrapper<GuestbookMessage>()
                        .eq(GuestbookMessage::getPublished, 1)
                        .orderByDesc(GuestbookMessage::getCreatedAt)
                        .orderByDesc(GuestbookMessage::getId)))
                .build();
    }

    /**
     * 提交留言。
     *
     * @param message 留言内容。
     * @param request 当前请求。
     * @return 保存后的留言。
     */
    @Override
    public GuestbookMessage saveGuestbookMessage(GuestbookMessage message, HttpServletRequest request) {
        if (message == null) {
            throw new BusinessException("留言内容不能为空");
        }
        String content = message.getContent() == null ? "" : message.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException("留言内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException("留言内容不能超过 2000 个字符");
        }
        GuestbookMessage nextMessage = new GuestbookMessage();
        nextMessage.setContent(XssUtil.cleanText(content));
        nextMessage.setPublished(message.getPublished() != null && message.getPublished() == 1 ? 1 : 0);
        nextMessage.setIpAddress(ClientIpUtil.resolveClientIp(request));
        nextMessage.setCreatedAt(LocalDateTime.now());
        nextMessage.setUpdatedAt(nextMessage.getCreatedAt());
        dyxGuestbookMessageMapper.insert(nextMessage);
        return nextMessage;
    }

    /**
     * 获取已发布文章列表。
     *
     * @return 文章列表。
     */
    @Override
    @Cacheable(value = "site", key = "'posts'")
    public List<Post> listPosts(Integer page, Integer pageSize) {
        int pageNo = normalizePage(page);
        int size = normalizePageSize(pageSize);
        List<Post> posts = dyxPostMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getPublished, 1)
                .orderByDesc(Post::getUpdatedAt));
        int fromIndex = (pageNo - 1) * size;
        if (fromIndex >= posts.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, posts.size());
        return posts.subList(fromIndex, toIndex);
    }

    /**
     * 获取文章详情。
     *
     * @param id 文章主键。
     * @return 文章详情对象。
     */
    @Override
    public Post getPostDetail(Long id) {
        Post post = dyxPostMapper.selectById(id);
        if (post == null || post.getPublished() == null || post.getPublished() != 1) {
            throw new BusinessException("文章不存在或未发布");
        }
        jdbcTemplate.update("UPDATE dyx_post SET view_count = COALESCE(view_count, 0) + 1 WHERE id = ?", id);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        return post;
    }

    /**
     * 获取已发布动态列表。
     *
     * @return 动态列表。
     */
    @Override
    @Cacheable(value = "site", key = "'moments'")
    public List<Moment> listMoments() {
        return dyxMomentMapper.selectList(new LambdaQueryWrapper<Moment>()
                .eq(Moment::getPublished, 1)
                .orderByDesc(Moment::getHappenedAt));
    }

    /**
     * 获取已发布动态详情。
     *
     * @param id 动态主键。
     * @return 动态详情。
     */
    @Override
    public Moment getMomentDetail(Long id) {
        Moment moment = dyxMomentMapper.selectOne(new LambdaQueryWrapper<Moment>()
                .eq(Moment::getId, id)
                .eq(Moment::getPublished, 1)
                .last("LIMIT 1"));
        if (moment == null) {
            throw new BusinessException("动态不存在或未发布");
        }
        return moment;
    }

    /**
     * 获取已发布项目经历列表。
     *
     * @return 项目经历列表。
     */
    @Override
    @Cacheable(value = "site", key = "'projects'")
    public List<Project> listProjects() {
        List<Project> projects = dyxProjectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getPublished, 1)
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getUpdatedAt));
        projects.forEach(project -> project.setProjectLink(normalizeExternalUrl(project.getProjectLink())));
        return projects;
    }

    /**
     * 获取已发布个人作品列表。
     *
     * @return 作品列表。
     */
    @Override
    @Cacheable(value = "site", key = "'works'")
    public List<Work> listWorks() {
        List<Work> works = dyxWorkMapper.selectList(new LambdaQueryWrapper<Work>()
                .eq(Work::getPublished, 1)
                .orderByDesc(Work::getAwardAt)
                .orderByAsc(Work::getSortOrder)
                .orderByDesc(Work::getUpdatedAt));
        works.forEach(work -> {
            work.setWorkLink(normalizeExternalUrl(work.getWorkLink()));
            work.setVideoUrl(normalizeVideoUrl(work.getVideoUrl()));
        });
        return works;
    }

    /**
     * 获取已发布荣誉列表。
     *
     * @return 荣誉列表。
     */
    @Override
    @Cacheable(value = "site", key = "'honors'")
    public List<Honor> listHonors() {
        return dyxHonorMapper.selectList(new LambdaQueryWrapper<Honor>()
                .eq(Honor::getPublished, 1)
                .orderByDesc(Honor::getAwardAt));
    }

    /**
     * 获取已发布首页足迹列表。
     *
     * @return 足迹列表。
     */
    @Override
    @Cacheable(value = "site", key = "'footprints'")
    public List<Footprint> listFootprints() {
        return dyxFootprintMapper.selectList(new LambdaQueryWrapper<Footprint>()
                .eq(Footprint::getPublished, 1)
                .orderByDesc(Footprint::getImportance)
                .orderByAsc(Footprint::getSortOrder)
                .orderByDesc(Footprint::getVisitedAt)
                .orderByDesc(Footprint::getUpdatedAt));
    }

    private String resolvePublicResumePdfUrl(String resumePdfUrl) {
        if (isBlank(resumePdfUrl)) {
            return resumePdfUrl;
        }
        if (resumePdfUrl.startsWith("/api/dyx-manager/media/content")) {
            return resumePdfUrl;
        }
        return "/api/dyx-manager/media/content?fileUrl="
                + URLEncoder.encode(resumePdfUrl, StandardCharsets.UTF_8);
    }

    private String normalizeExternalUrl(String value) {
        if (isBlank(value)) {
            return value;
        }
        String normalized = value.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        if (normalized.startsWith("//")) {
            return "https:" + normalized;
        }
        return "https://" + normalized;
    }

    private String normalizeVideoUrl(String value) {
        if (isBlank(value)) {
            return value;
        }
        String normalized = value.trim();
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (lowerCase.matches(".*\\.(mp4|webm|mov|m4v)(?:\\?.*)?$")) {
            return normalized;
        }
        return null;
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return 1;
        }
        if (page < 1) {
            throw new BusinessException("page 必须大于等于 1");
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return 20;
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException("pageSize 需在 1 到 100 之间");
        }
        return pageSize;
    }

    private Map<String, Object> getHomeSystemConfig() {
        SystemConfig systemConfig = dyxSystemConfigMapper.selectById(1L);
        Map<String, Object> result = new HashMap<>();
        result.put("footprintEyebrow", systemConfig == null ? null : systemConfig.getFootprintEyebrow());
        result.put("footprintTitle", systemConfig == null ? null : systemConfig.getFootprintTitle());
        result.put("footprintSubtitle", systemConfig == null ? null : systemConfig.getFootprintSubtitle());
        result.put("footprintDescription", systemConfig == null ? null : systemConfig.getFootprintDescription());
        result.put("copyrightText", systemConfig == null ? null : systemConfig.getCopyrightText());
        result.put("techSupportText", systemConfig == null ? null : systemConfig.getTechSupportText());
        return result;
    }

    /**
     * 记录公开页面访问。
     *
     * @param pageKey 页面标识。
     * @param request 当前请求。
     */
    @Override
    public void recordSiteVisit(String pageKey, HttpServletRequest request) {
        try {
            String normalizedPageKey = normalizePageKey(pageKey);
            String clientIp = ClientIpUtil.resolveClientIp(request);
            String userAgent = resolveUserAgent(request);

            SiteVisitLog visitLog = new SiteVisitLog();
            visitLog.setPageKey(normalizedPageKey);
            visitLog.setIpAddress(clientIp);
            visitLog.setUserAgent(userAgent);
            visitLog.setDeviceType(resolveDeviceType(userAgent));
            visitLog.setDeviceName(resolveDeviceName(userAgent));
            visitLog.setCreatedAt(LocalDateTime.now());

            dyxSiteVisitLogMapper.insert(visitLog);

            // 异步累加统计
            jdbcTemplate.update(
                    "INSERT INTO dyx_site_visit_stat (page_key, visit_count, updated_at) VALUES (?, 1, NOW()) " +
                            "ON DUPLICATE KEY UPDATE visit_count = visit_count + 1, updated_at = NOW()",
                    normalizedPageKey);

        } catch (Exception e) {
            log.error("记录访问日志失败 [{}]: {}", pageKey, e.getMessage());
        }
    }

    private String normalizePageKey(String pageKey) {
        String normalized = pageKey == null ? "" : pageKey.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("页面标识不能为空");
        }
        if (normalized.length() > 64) {
            throw new BusinessException("页面标识长度不能超过 64 个字符");
        }
        return normalized;
    }

    private String resolveUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return isBlank(userAgent) ? "unknown" : truncate(userAgent.trim(), 512);
    }

    private String resolveDeviceType(String userAgent) {
        String normalizedUserAgent = normalizeUserAgent(userAgent);
        if (containsBotKeyword(normalizedUserAgent)) {
            return "BOT";
        }
        if (normalizedUserAgent.contains("ipad") || normalizedUserAgent.contains("tablet")) {
            return "TABLET";
        }
        if (normalizedUserAgent.contains("iphone")
                || normalizedUserAgent.contains("android")
                || normalizedUserAgent.contains("mobile")) {
            return "MOBILE";
        }
        if (normalizedUserAgent.contains("windows")
                || normalizedUserAgent.contains("macintosh")
                || normalizedUserAgent.contains("linux")
                || normalizedUserAgent.contains("x11")
                || normalizedUserAgent.contains("cros")) {
            return "DESKTOP";
        }
        return "UNKNOWN";
    }

    private String resolveDeviceName(String userAgent) {
        String normalizedUserAgent = normalizeUserAgent(userAgent);
        if (containsBotKeyword(normalizedUserAgent)) {
            if (normalizedUserAgent.contains("googlebot")) {
                return "Googlebot";
            }
            if (normalizedUserAgent.contains("bingbot")) {
                return "Bingbot";
            }
            if (normalizedUserAgent.contains("bytespider")) {
                return "Bytespider";
            }
            return "Bot";
        }
        if (normalizedUserAgent.contains("iphone")) {
            return "iPhone";
        }
        if (normalizedUserAgent.contains("ipad")) {
            return "iPad";
        }
        Matcher matcher = ANDROID_DEVICE_PATTERN.matcher(userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT));
        if (matcher.find()) {
            return truncate(matcher.group(1).trim(), 128);
        }
        if (normalizedUserAgent.contains("android")) {
            return "Android";
        }
        if (normalizedUserAgent.contains("windows")) {
            return "Windows";
        }
        if (normalizedUserAgent.contains("macintosh") || normalizedUserAgent.contains("mac os")) {
            return "Mac";
        }
        if (normalizedUserAgent.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    private String normalizeUserAgent(String userAgent) {
        return userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
    }

    private boolean containsBotKeyword(String normalizedUserAgent) {
        return normalizedUserAgent.contains("bot")
                || normalizedUserAgent.contains("crawler")
                || normalizedUserAgent.contains("spider")
                || normalizedUserAgent.contains("slurp")
                || normalizedUserAgent.contains("curl")
                || normalizedUserAgent.contains("wget");
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
