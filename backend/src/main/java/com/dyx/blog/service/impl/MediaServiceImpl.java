package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.config.FileProperties;
import com.dyx.blog.entity.Honor;
import com.dyx.blog.entity.Media;
import com.dyx.blog.entity.Moment;
import com.dyx.blog.entity.Post;
import com.dyx.blog.entity.Profile;
import com.dyx.blog.entity.Project;
import com.dyx.blog.entity.SystemConfig;
import com.dyx.blog.entity.Work;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MediaMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.WorkMapper;
import com.dyx.blog.service.AdminService;
import com.dyx.blog.service.MediaService;
import com.dyx.blog.storage.MediaStorage;
import com.dyx.blog.storage.MediaStorageResult;
import com.dyx.blog.storage.OssMediaStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 媒体资源服务实现类。
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> IMPORTABLE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf", "mp4", "webm", "mov", "m4v"
    );
    private static final long MAX_UPLOAD_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_UPLOAD_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf", "mp4", "webm", "mov", "m4v"
    );
    private static final Set<String> ALLOWED_UPLOAD_MEDIA_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "application/pdf", "video/mp4", "video/webm", "video/quicktime", "video/x-m4v"
    );
    private static final Set<String> IMAGE_MEDIA_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    private static final Set<String> PRIVATE_IPV4_PREFIXES = Set.of(
            "10.", "127.", "169.254.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.", "172.26.",
            "172.27.", "172.28.", "172.29.", "172.30.", "172.31.", "192.168."
    );

    private final MediaMapper dyxMediaMapper;
    private final ProfileMapper dyxProfileMapper;
    private final PostMapper dyxPostMapper;
    private final ProjectMapper dyxProjectMapper;
    private final WorkMapper dyxWorkMapper;
    private final MomentMapper dyxMomentMapper;
    private final HonorMapper dyxHonorMapper;
    private final FileProperties dyxFileProperties;
    private final AdminService dyxAdminService;
    private final List<MediaStorage> mediaStorages;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 上传媒体文件。
     *
     * @param file 上传文件。
     * @return 保存后的媒体资源对象。
     */
    @Override
    public Media upload(MultipartFile file) {
        validateUpload(file);
        String originalFilename = file.getOriginalFilename();
        String extension = normalizeExtension(StringUtils.getFilenameExtension(originalFilename));
        String storedFileName = UUID.randomUUID() + "." + extension;
        MediaStorageResult storageResult = resolveCurrentStorage().upload(file, storedFileName);

        Media media = new Media();
        media.setOriginalName(sanitizeOriginalFilename(originalFilename));
        media.setFileName(storageResult.fileName());
        media.setFileUrl(storageResult.fileUrl());
        media.setMediaType(normalizeMediaType(file.getContentType()));
        media.setFileSize(file.getSize());
        media.setCreatedAt(LocalDateTime.now());
        dyxMediaMapper.insert(media);
        return media;
    }

    /**
     * 导入 uploads 目录下已存在的文件。
     *
     * @return 导入数量。
     */
    @Override
    public int importExistingFiles() {
        if (!"local".equals(resolveCurrentStorage().getStorageType())) {
            throw new BusinessException("当前存储模式不支持导入本地 uploads 目录");
        }
        try {
            Path uploadDirectory = getUploadDirectory();
            Set<String> existingFileNames = dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                            .select(Media::getFileName))
                    .stream()
                    .map(Media::getFileName)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());

            int importedCount = 0;
            try (Stream<Path> stream = Files.walk(uploadDirectory, 1)) {
                List<Path> filePaths = stream
                        .filter(Files::isRegularFile)
                        .toList();
                for (Path filePath : filePaths) {
                    String fileName = filePath.getFileName().toString();
                    String extension = StringUtils.getFilenameExtension(fileName);
                    if (extension == null || !IMPORTABLE_EXTENSIONS.contains(extension.toLowerCase())) {
                        continue;
                    }
                    if (existingFileNames.contains(fileName)) {
                        continue;
                    }
                    Media media = new Media();
                    media.setOriginalName(fileName);
                    media.setFileName(fileName);
                    String detectedMediaType = normalizeMediaType(Files.probeContentType(filePath));
                    validateImportedFile(filePath, detectedMediaType);
                    media.setFileUrl(buildLocalFileUrl(fileName));
                    media.setMediaType(detectedMediaType);
                    media.setFileSize(Files.size(filePath));
                    media.setCreatedAt(LocalDateTime.now());
                    dyxMediaMapper.insert(media);
                    importedCount++;
                }
            }
            return importedCount;
        } catch (IOException exception) {
            throw new BusinessException("导入已有文件失败");
        }
    }

    /**
     * 查询全部媒体资源。
     *
     * @return 媒体资源列表。
     */
    @Override
    public List<Media> listAll() {
        syncCurrentStorageMedia();
        return dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                .orderByDesc(Media::getCreatedAt));
    }

    @Override
    public ResponseEntity<byte[]> proxyMedia(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException("媒体资源链接无效");
        }
        Media media = dyxMediaMapper.selectOne(new LambdaQueryWrapper<Media>()
                .eq(Media::getFileUrl, fileUrl)
                .last("limit 1"));
        if (media == null) {
            throw new BusinessException("媒体资源不存在");
        }
        if (fileUrl.startsWith(normalizeAccessPrefix())) {
            return proxyLocalMedia(media);
        }
        return proxyRemoteMedia(media);
    }


    /**
     * 删除未被引用的媒体资源。
     *
     * @param id 媒体主键。
     */
    @Override
    public void deleteById(Long id) {
        Media media = dyxMediaMapper.selectById(id);
        if (media == null) {
            throw new BusinessException("媒体资源不存在");
        }
        String fileUrl = media.getFileUrl();
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException("媒体资源链接无效，无法删除");
        }
        String referenceModule = resolveReferenceModule(fileUrl);
        if (referenceModule != null) {
            throw new BusinessException("该媒体仍被" + referenceModule + "引用，请先解除引用后再删除");
        }
        resolveStorageForMedia(media).delete(media.getFileName(), media.getFileUrl());
        dyxMediaMapper.deleteById(id);
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            throw new BusinessException("上传文件不能超过 20MB");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = normalizeExtension(StringUtils.getFilenameExtension(originalFilename));
        if (!ALLOWED_UPLOAD_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型，仅允许上传图片、PDF 或常见视频文件");
        }
        String mediaType = normalizeMediaType(file.getContentType());
        if (!ALLOWED_UPLOAD_MEDIA_TYPES.contains(mediaType)) {
            throw new BusinessException("检测到不安全的文件类型，上传已拒绝");
        }
        if (IMAGE_MEDIA_TYPES.contains(mediaType)) {
            validateImageContent(file);
        }
    }

    private void validateImportedFile(Path filePath, String mediaType) {
        try {
            long fileSize = Files.size(filePath);
            if (fileSize <= 0) {
                throw new BusinessException("检测到空文件，禁止导入");
            }
            if (fileSize > MAX_UPLOAD_SIZE) {
                throw new BusinessException("检测到超出限制的文件，禁止导入");
            }
            if (!ALLOWED_UPLOAD_MEDIA_TYPES.contains(mediaType)) {
                throw new BusinessException("检测到不安全的本地文件类型，禁止导入");
            }
            if (IMAGE_MEDIA_TYPES.contains(mediaType)) {
                validateImageContent(filePath);
            }
        } catch (IOException exception) {
            throw new BusinessException("校验本地文件失败，无法导入");
        }
    }

    private void validateImageContent(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            if (ImageIO.read(inputStream) == null) {
                throw new BusinessException("图片内容无效或已损坏");
            }
        } catch (IOException exception) {
            throw new BusinessException("图片内容校验失败");
        }
    }

    private void validateImageContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            if (ImageIO.read(inputStream) == null) {
                throw new BusinessException("图片内容无效或已损坏");
            }
        } catch (IOException exception) {
            throw new BusinessException("图片内容校验失败");
        }
    }

    private String normalizeExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            throw new BusinessException("文件缺少合法扩展名");
        }
        return extension.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMediaType(String mediaType) {
        if (!StringUtils.hasText(mediaType)) {
            throw new BusinessException("无法识别文件类型，请更换文件后重试");
        }
        return mediaType.trim().toLowerCase(Locale.ROOT);
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "未命名文件";
        }
        return originalFilename.replace("\r", "").replace("\n", "").trim();
    }

    private ResponseEntity<byte[]> proxyLocalMedia(Media media) {
        try {
            Path path = resolveLocalFilePath(media);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new BusinessException("媒体文件不存在");
            }
            byte[] bytes = Files.readAllBytes(path);
            String contentType = resolveMediaType(media, Files.probeContentType(path));
            return buildMediaResponse(bytes, contentType, media.getOriginalName(), path.getFileName().toString());
        } catch (IOException exception) {
            throw new BusinessException("读取本地媒体文件失败");
        }
    }

    private ResponseEntity<byte[]> proxyRemoteMedia(Media media) {
        URI remoteUri = validateRemoteMediaUri(media.getFileUrl());
        try {
            ResponseEntity<ByteArrayResource> response = restTemplate.getForEntity(remoteUri, ByteArrayResource.class);
            ByteArrayResource body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new BusinessException("读取远程媒体文件失败");
            }
            String contentType = resolveMediaType(media, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
            return buildMediaResponse(body.getByteArray(), contentType, media.getOriginalName(), media.getFileName());
        } catch (RestClientException exception) {
            throw new BusinessException("读取远程媒体文件失败");
        }
    }

    private ResponseEntity<byte[]> buildMediaResponse(byte[] bytes, String contentType, String originalName, String fallbackName) {
        String fileName = StringUtils.hasText(originalName) ? originalName : fallbackName;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeFileName(fileName) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private String resolveMediaType(Media media, String fallbackType) {
        if (StringUtils.hasText(media.getMediaType())) {
            return media.getMediaType();
        }
        if (StringUtils.hasText(fallbackType)) {
            return fallbackType;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private Path resolveLocalFilePath(Media media) throws IOException {
        Path uploadDirectory = getUploadDirectory();
        String normalizedPrefix = normalizeAccessPrefix();
        String relativePath = StringUtils.hasText(media.getFileUrl()) && media.getFileUrl().startsWith(normalizedPrefix)
                ? media.getFileUrl().substring(normalizedPrefix.length())
                : media.getFileName();
        if (!StringUtils.hasText(relativePath)) {
            throw new BusinessException("媒体资源路径无效");
        }
        Path resolvedPath = uploadDirectory.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(uploadDirectory)) {
            throw new BusinessException("媒体资源路径无效");
        }
        return resolvedPath;
    }

    private URI validateRemoteMediaUri(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException("媒体资源链接无效");
        }
        try {
            URI uri = new URI(fileUrl.trim()).normalize();
            String scheme = uri.getScheme();
            if (!"https".equalsIgnoreCase(scheme)) {
                throw new BusinessException("远程媒体链接仅支持 HTTPS");
            }
            if (uri.getUserInfo() != null || uri.getHost() == null || uri.getPort() != -1) {
                throw new BusinessException("远程媒体链接无效");
            }
            String host = IDN.toASCII(uri.getHost().trim()).toLowerCase(Locale.ROOT);
            if (isDisallowedRemoteHost(host)) {
                throw new BusinessException("远程媒体链接不受信任");
            }
            return new URI(uri.getScheme(), null, host, -1, uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException exception) {
            throw new BusinessException("远程媒体链接无效");
        }
    }

    private boolean isDisallowedRemoteHost(String host) {
        if (!StringUtils.hasText(host)) {
            return true;
        }
        if ("localhost".equals(host) || host.endsWith(".localhost") || host.endsWith(".local")) {
            return true;
        }
        if (host.contains(":")) {
            String normalized = host;
            if (normalized.startsWith("[") && normalized.endsWith("]")) {
                normalized = normalized.substring(1, normalized.length() - 1);
            }
            return "::1".equals(normalized)
                    || normalized.startsWith("fc")
                    || normalized.startsWith("fd")
                    || normalized.startsWith("fe80:");
        }
        for (String prefix : PRIVATE_IPV4_PREFIXES) {
            if (host.startsWith(prefix)) {
                return true;
            }
        }
        if (isIpv4Address(host)) {
            try {
                return InetAddress.getByName(host).isAnyLocalAddress()
                        || InetAddress.getByName(host).isLoopbackAddress()
                        || InetAddress.getByName(host).isSiteLocalAddress()
                        || InetAddress.getByName(host).isLinkLocalAddress()
                        || InetAddress.getByName(host).isMulticastAddress();
            } catch (IOException exception) {
                return true;
            }
        }
        return false;
    }

    private boolean isIpv4Address(String host) {
        String[] parts = host.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }
            for (int index = 0; index < part.length(); index++) {
                if (!Character.isDigit(part.charAt(index))) {
                    return false;
                }
            }
            int value = Integer.parseInt(part);
            if (value < 0 || value > 255) {
                return false;
            }
        }
        return true;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replace("\r", "").replace("\n", "").replace("\"", "");
    }

    private Path getUploadDirectory() throws IOException {
        Path uploadDirectory = Paths.get(dyxFileProperties.getUploadPath()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        return uploadDirectory;
    }

    private String buildLocalFileUrl(String fileName) {
        return normalizeAccessPrefix() + fileName;
    }

    private void cleanupMissingMediaRecords() {
        List<Media> mediaList = dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                .select(Media::getId, Media::getFileName, Media::getFileUrl));
        for (Media media : mediaList) {
            if (storedFileExists(media)) {
                continue;
            }
            dyxMediaMapper.deleteById(media.getId());
        }
    }

    private boolean storedFileExists(Media media) {
        return resolveStorageForMedia(media).exists(media.getFileName(), media.getFileUrl());
    }

    private void syncCurrentStorageMedia() {
        MediaStorage currentStorage = resolveCurrentStorage();
        if ("local".equals(currentStorage.getStorageType())) {
            cleanupMissingMediaRecords();
            return;
        }
        if (!(currentStorage instanceof OssMediaStorage ossMediaStorage)) {
            return;
        }
        syncOssMediaRecords(ossMediaStorage);
    }

    private void syncOssMediaRecords(OssMediaStorage ossMediaStorage) {
        Set<String> existingFileNames = dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                        .select(Media::getFileName))
                .stream()
                .map(Media::getFileName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
        for (OssMediaStorage.StoredObject storedObject : ossMediaStorage.listStoredObjects()) {
            if (!StringUtils.hasText(storedObject.objectKey()) || existingFileNames.contains(storedObject.objectKey())) {
                continue;
            }
            Media media = new Media();
            media.setOriginalName(StringUtils.hasText(storedObject.originalName()) ? storedObject.originalName() : storedObject.objectKey());
            media.setFileName(storedObject.objectKey());
            media.setFileUrl(storedObject.fileUrl());
            media.setMediaType(storedObject.contentType());
            media.setFileSize(storedObject.fileSize());
            media.setCreatedAt(storedObject.lastModifiedAt() != null ? storedObject.lastModifiedAt() : LocalDateTime.now());
            dyxMediaMapper.insert(media);
            existingFileNames.add(storedObject.objectKey());
        }
    }

    private String normalizeAccessPrefix() {
        String accessPrefix = dyxFileProperties.getAccessPrefix();
        if (!StringUtils.hasText(accessPrefix)) {
            return "/";
        }
        return accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
    }

    private MediaStorage resolveCurrentStorage() {
        SystemConfig systemConfig = dyxAdminService.getSystemConfig();
        String storageType = systemConfig == null || !StringUtils.hasText(systemConfig.getStorageType())
                ? dyxFileProperties.getStorageType()
                : systemConfig.getStorageType();
        return requireStorage(storageType);
    }

    private MediaStorage resolveStorageForMedia(Media media) {
        String fileUrl = media.getFileUrl();
        if (StringUtils.hasText(fileUrl) && fileUrl.startsWith(normalizeAccessPrefix())) {
            return requireStorage("local");
        }
        if (StringUtils.hasText(fileUrl) && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            return requireStorage("oss");
        }
        return resolveCurrentStorage();
    }

    private MediaStorage requireStorage(String storageType) {
        String normalizedStorageType = StringUtils.hasText(storageType) ? storageType.trim().toLowerCase() : "local";
        return mediaStorages.stream()
                .filter(storage -> normalizedStorageType.equals(storage.getStorageType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到可用的媒体存储实现：" + normalizedStorageType));
    }

    private String resolveReferenceModule(String fileUrl) {
        if (isProfileReferenced(fileUrl)) {
            return "个人资料";
        }
        if (existsExactReference(dyxPostMapper, Post::getCoverImage, fileUrl)) {
            return "文章封面";
        }
        if (existsExactReference(dyxProjectMapper, Project::getCoverImage, fileUrl)) {
            return "项目经历";
        }
        if (existsExactReference(dyxWorkMapper, Work::getCoverImage, fileUrl)
                || existsContainsReference(dyxWorkMapper, Work::getImageUrls, fileUrl)
                || existsExactReference(dyxWorkMapper, Work::getVideoUrl, fileUrl)
                || existsExactReference(dyxWorkMapper, Work::getVideoPoster, fileUrl)) {
            return "个人作品";
        }
        if (existsExactReference(dyxMomentMapper, Moment::getCoverImage, fileUrl) || existsContainsReference(dyxMomentMapper, Moment::getImageUrls, fileUrl)) {
            return "动态内容";
        }
        if (existsExactReference(dyxHonorMapper, Honor::getCoverImage, fileUrl)
                || existsContainsReference(dyxHonorMapper, Honor::getImageUrls, fileUrl)
                || existsExactReference(dyxHonorMapper, Honor::getAttachmentUrl, fileUrl)) {
            return "荣誉内容";
        }
        return null;
    }

    private boolean isProfileReferenced(String fileUrl) {
        return dyxProfileMapper.selectCount(new LambdaQueryWrapper<Profile>()
                        .eq(Profile::getAvatarUrl, fileUrl)
                        .or()
                        .eq(Profile::getResumePdfUrl, fileUrl)) > 0;
    }

    private <T> boolean existsExactReference(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
                                             com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, String> column,
                                             String fileUrl) {
        return mapper.selectCount(new LambdaQueryWrapper<T>().eq(column, fileUrl)) > 0;
    }

    private <T> boolean existsContainsReference(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
                                                com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, String> column,
                                                String fileUrl) {
        return mapper.selectCount(new LambdaQueryWrapper<T>().like(column, fileUrl)) > 0;
    }
}
