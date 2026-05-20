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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 媒体资源服务实现类。
 * <p>
 * 负责媒体上传、历史文件导入、媒体代理读取、引用校验和多存储实现切换。
 * 同时包含对文件类型、图片内容、远程地址安全性和引用关系的统一校验逻辑。
 * </p>
 *
 * @author Dyx
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> IMPORTABLE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf", "mp4", "webm", "mov", "m4v"
    );
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
    private static final AtomicBoolean IMAGE_IO_PLUGINS_SCANNED = new AtomicBoolean();

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

    @Override
    @Transactional
    public Media registerRemoteMedia(String fileUrl, String originalName) {
        URI remoteUri = validateRemoteMediaUri(fileUrl);
        String normalizedFileUrl = remoteUri.toString();
        Media existingMedia = dyxMediaMapper.selectOne(new LambdaQueryWrapper<Media>()
                .eq(Media::getFileUrl, normalizedFileUrl)
                .last("limit 1"));
        if (existingMedia != null) {
            return existingMedia;
        }
        String fallbackFileName = extractRemoteFileName(remoteUri);
        Media media = new Media();
        media.setOriginalName(resolveRemoteOriginalName(originalName, fallbackFileName));
        media.setFileName(fallbackFileName);
        media.setFileUrl(normalizedFileUrl);
        media.setMediaType(resolveRemoteMediaType(normalizedFileUrl));
        media.setFileSize(0L);
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
    public ResponseEntity<byte[]> proxyMedia(String fileUrl, String range) {
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
            return proxyLocalMedia(media, range);
        }
        return proxyRemoteMedia(media, range);
    }


    /**
     * 删除未被引用的媒体资源。
     *
     * @param id 媒体主键。
     */
    @Override
    public void deleteById(Long id) {
        Media media = dyxMediaMapper.selectById(id);
        deleteMedia(media);
    }

    @Override
    public void deleteBatchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择需要删除的媒体文件");
        }
        for (Long id : ids) {
            Media media = dyxMediaMapper.selectById(id);
            deleteMedia(media);
        }
    }

    private void deleteMedia(Media media) {
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
        if (!isExternalMedia(media)) {
            resolveStorageForMedia(media).delete(media.getFileName(), media.getFileUrl());
        }
        dyxMediaMapper.deleteById(media.getId());
    }

    /**
     * 校验上传文件的大小、扩展名、媒体类型与图片内容。
     * <p>
     * 对图片文件会进一步尝试解析真实内容，避免伪造扩展名或损坏文件进入媒体库。
     * </p>
     *
     * @param file 待上传的媒体文件。
     * @return 无返回值。
     * @throws BusinessException 当文件为空、超出大小限制、类型不受支持或图片内容校验失败时抛出。
     * @author Dyx
     */
    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        long maxSize = dyxFileProperties.getMaxUploadSizeBytes();
        if (file.getSize() > maxSize) {
            long maxMb = Math.max(1, maxSize / (1024 * 1024));
            throw new BusinessException("上传文件不能超过 " + maxMb + "MB");
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

    /**
     * 校验本地导入文件的大小、媒体类型与图片内容是否合法。
     *
     * @param filePath  待导入的文件路径。
     * @param mediaType 文件探测得到的媒体类型。
     * @return 无返回值。
     * @throws BusinessException 当文件为空、超限、类型不安全或图片内容校验失败时抛出。
     * @author Dyx
     */
    private void validateImportedFile(Path filePath, String mediaType) {
        try {
            long fileSize = Files.size(filePath);
            if (fileSize <= 0) {
                throw new BusinessException("检测到空文件，禁止导入");
            }
            long maxSize = dyxFileProperties.getMaxUploadSizeBytes();
            if (fileSize > maxSize) {
                long maxMb = Math.max(1, maxSize / (1024 * 1024));
                throw new BusinessException("检测到超出限制的文件（超过 " + maxMb + "MB），禁止导入");
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
            ensureImageIoPluginsLoaded();
            if (ImageIO.read(inputStream) == null) {
                throw new BusinessException("图片内容无效或已损坏");
            }
        } catch (IOException exception) {
            throw new BusinessException("图片内容校验失败");
        }
    }

    private void validateImageContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ensureImageIoPluginsLoaded();
            if (ImageIO.read(inputStream) == null) {
                throw new BusinessException("图片内容无效或已损坏");
            }
        } catch (IOException exception) {
            throw new BusinessException("图片内容校验失败");
        }
    }

    private void ensureImageIoPluginsLoaded() {
        if (IMAGE_IO_PLUGINS_SCANNED.compareAndSet(false, true)) {
            ImageIO.scanForPlugins();
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

    /**
     * 代理读取本地存储中的媒体文件，并返回适合浏览器内联展示的响应。
     *
     * @param media 媒体记录对象。
     * @return 包含媒体字节内容的 HTTP 响应。
     * @throws BusinessException 当本地文件不存在或读取失败时抛出。
     * @author Dyx
     */
    private ResponseEntity<byte[]> proxyLocalMedia(Media media, String range) {
        try {
            Path path = resolveLocalFilePath(media);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new BusinessException("媒体文件不存在");
            }
            long fileLength = Files.size(path);
            String contentType = resolveMediaType(media, Files.probeContentType(path));
            String fallbackName = path.getFileName().toString();
            if (!StringUtils.hasText(range)) {
                byte[] bytes = Files.readAllBytes(path);
                return buildMediaResponse(bytes, contentType, media.getOriginalName(), fallbackName);
            }
            ByteRange byteRange = parseByteRange(range, fileLength);
            return buildPartialLocalMediaResponse(path, contentType, media.getOriginalName(), fallbackName, byteRange, fileLength);
        } catch (IOException exception) {
            throw new BusinessException("读取本地媒体文件失败");
        }
    }

    /**
     * 代理读取远程存储中的媒体文件，并对远程地址安全性进行前置校验。
     *
     * @param media 媒体记录对象。
     * @return 包含媒体字节内容的 HTTP 响应。
     * @throws BusinessException 当远程地址不受信任、请求失败或响应内容异常时抛出。
     * @author Dyx
     */
    private ResponseEntity<byte[]> proxyRemoteMedia(Media media, String range) {
        URI remoteUri = validateRemoteMediaUri(media.getFileUrl());
        try {
            HttpHeaders headers = new HttpHeaders();
            if (StringUtils.hasText(range)) {
                headers.set(HttpHeaders.RANGE, range.trim());
            }
            ResponseEntity<ByteArrayResource> response = restTemplate.exchange(
                    remoteUri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ByteArrayResource.class);
            ByteArrayResource body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new BusinessException("读取远程媒体文件失败");
            }
            String contentType = resolveMediaType(media, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
            return buildRemoteMediaResponse(body.getByteArray(), contentType, media.getOriginalName(), media.getFileName(), response);
        } catch (RestClientException exception) {
            throw new BusinessException("读取远程媒体文件失败");
        }
    }

    private ResponseEntity<byte[]> buildMediaResponse(byte[] bytes, String contentType, String originalName, String fallbackName) {
        String fileName = StringUtils.hasText(originalName) ? originalName : fallbackName;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeFileName(fileName) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private ResponseEntity<byte[]> buildPartialLocalMediaResponse(Path path,
                                                                  String contentType,
                                                                  String originalName,
                                                                  String fallbackName,
                                                                  ByteRange byteRange,
                                                                  long fileLength) throws IOException {
        int length = Math.toIntExact(byteRange.end() - byteRange.start() + 1);
        byte[] bytes = new byte[length];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r")) {
            randomAccessFile.seek(byteRange.start());
            randomAccessFile.readFully(bytes);
        }
        return buildPartialMediaResponse(bytes, contentType, originalName, fallbackName, byteRange, fileLength);
    }

    private ResponseEntity<byte[]> buildRemoteMediaResponse(byte[] bytes,
                                                            String contentType,
                                                            String originalName,
                                                            String fallbackName,
                                                            ResponseEntity<ByteArrayResource> response) {
        String contentRange = response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE);
        if (!StringUtils.hasText(contentRange)) {
            return buildMediaResponse(bytes, contentType, originalName, fallbackName);
        }
        ByteRange byteRange = parseContentRange(contentRange, bytes.length);
        long totalLength = resolveTotalLength(contentRange, bytes.length);
        return buildPartialMediaResponse(bytes, contentType, originalName, fallbackName, byteRange, totalLength);
    }

    private ResponseEntity<byte[]> buildPartialMediaResponse(byte[] bytes,
                                                             String contentType,
                                                             String originalName,
                                                             String fallbackName,
                                                             ByteRange byteRange,
                                                             long fileLength) {
        String fileName = StringUtils.hasText(originalName) ? originalName : fallbackName;
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + byteRange.start() + "-" + byteRange.end() + "/" + fileLength)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeFileName(fileName) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(bytes.length)
                .body(bytes);
    }

    private ByteRange parseByteRange(String range, long fileLength) {
        String normalized = range == null ? "" : range.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("bytes=")) {
            throw new BusinessException("不支持的媒体范围请求");
        }
        String value = normalized.substring("bytes=".length()).trim();
        if (value.isEmpty() || value.contains(",")) {
            throw new BusinessException("不支持的媒体范围请求");
        }
        int separatorIndex = value.indexOf('-');
        if (separatorIndex < 0) {
            throw new BusinessException("不支持的媒体范围请求");
        }
        String startPart = value.substring(0, separatorIndex).trim();
        String endPart = value.substring(separatorIndex + 1).trim();
        try {
            long start;
            long end;
            if (startPart.isEmpty()) {
                long suffixLength = Long.parseLong(endPart);
                if (suffixLength <= 0) {
                    throw new BusinessException("不支持的媒体范围请求");
                }
                long effectiveLength = Math.min(suffixLength, fileLength);
                start = fileLength - effectiveLength;
                end = fileLength - 1;
            } else {
                start = Long.parseLong(startPart);
                end = endPart.isEmpty() ? fileLength - 1 : Long.parseLong(endPart);
            }
            if (fileLength <= 0 || start < 0 || end < start || start >= fileLength) {
                throw new BusinessException("媒体范围请求超出文件大小");
            }
            return new ByteRange(start, Math.min(end, fileLength - 1));
        } catch (NumberFormatException exception) {
            throw new BusinessException("不支持的媒体范围请求");
        }
    }

    private ByteRange parseContentRange(String contentRange, int defaultLength) {
        String normalized = contentRange == null ? "" : contentRange.trim();
        if (!normalized.startsWith("bytes ")) {
            return new ByteRange(0, Math.max(defaultLength - 1, 0));
        }
        int slashIndex = normalized.indexOf('/');
        int dashIndex = normalized.indexOf('-');
        if (dashIndex < 6 || slashIndex < 0 || dashIndex > slashIndex) {
            return new ByteRange(0, Math.max(defaultLength - 1, 0));
        }
        try {
            long start = Long.parseLong(normalized.substring(6, dashIndex).trim());
            long end = Long.parseLong(normalized.substring(dashIndex + 1, slashIndex).trim());
            if (start < 0 || end < start) {
                return new ByteRange(0, Math.max(defaultLength - 1, 0));
            }
            return new ByteRange(start, end);
        } catch (NumberFormatException exception) {
            return new ByteRange(0, Math.max(defaultLength - 1, 0));
        }
    }

    private long resolveTotalLength(String contentRange, int defaultLength) {
        String normalized = contentRange == null ? "" : contentRange.trim();
        int slashIndex = normalized.indexOf('/');
        if (slashIndex < 0 || slashIndex == normalized.length() - 1) {
            return defaultLength;
        }
        String totalPart = normalized.substring(slashIndex + 1).trim();
        if (!StringUtils.hasText(totalPart) || "*".equals(totalPart)) {
            return defaultLength;
        }
        try {
            long totalLength = Long.parseLong(totalPart);
            return totalLength > 0 ? totalLength : defaultLength;
        } catch (NumberFormatException exception) {
            return defaultLength;
        }
    }

    private record ByteRange(long start, long end) {
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

    /**
     * 校验远程媒体地址，仅允许 HTTPS 且拒绝本地、私网和回环地址。
     *
     * @param fileUrl 待校验的远程媒体地址。
     * @return 返回规范化后的远程 URI。
     * @throws BusinessException 当链接为空、格式非法或指向不受信任主机时抛出。
     * @author Dyx
     */
    private URI validateRemoteMediaUri(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            throw new BusinessException("媒体资源链接无效");
        }
        try {
            URI uri = new URI(fileUrl.trim()).normalize();
            String scheme = uri.getScheme();
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
                throw new BusinessException("远程媒体链接仅支持 HTTP 或 HTTPS");
            }
            if (uri.getUserInfo() != null || uri.getHost() == null || uri.getPort() != -1) {
                throw new BusinessException("远程媒体链接无效");
            }
            String host = IDN.toASCII(uri.getHost().trim()).toLowerCase(Locale.ROOT);
            if (isDisallowedRemoteHost(host)) {
                throw new BusinessException("远程媒体链接不受信任");
            }
            return new URI(uri.getScheme().toLowerCase(Locale.ROOT), null, host, -1, uri.getPath(), uri.getQuery(), null);
        } catch (URISyntaxException exception) {
            throw new BusinessException("远程媒体链接无效");
        }
    }

    /**
     * 判断远程主机是否属于应拒绝访问的本地或私网地址。
     *
     * @param host 远程主机名或 IP。
     * @return 命中本地、私网、链路本地或其他不受信任地址时返回 true。
     * @throws BusinessException 该方法本身不主动抛出业务异常；域名解析异常时按不受信任处理。
     * @author Dyx
     */
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

    private String resolveRemoteOriginalName(String originalName, String fallbackFileName) {
        if (StringUtils.hasText(originalName)) {
            return sanitizeOriginalFilename(originalName);
        }
        return sanitizeOriginalFilename(fallbackFileName);
    }

    private String extractRemoteFileName(URI remoteUri) {
        String path = remoteUri.getPath();
        if (StringUtils.hasText(path)) {
            int separatorIndex = path.lastIndexOf('/');
            String candidate = separatorIndex >= 0 ? path.substring(separatorIndex + 1) : path;
            if (StringUtils.hasText(candidate)) {
                return sanitizeOriginalFilename(candidate);
            }
        }
        String host = remoteUri.getHost();
        return StringUtils.hasText(host) ? sanitizeOriginalFilename(host) : "远程媒体";
    }

    private String resolveRemoteMediaType(String fileUrl) {
        String contentType = URLConnection.guessContentTypeFromName(fileUrl);
        return StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
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
            if (isExternalMedia(media)) {
                continue;
            }
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
                        .select(Media::getFileName, Media::getFileUrl))
                .stream()
                .filter(this::isManagedOssMedia)
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
        if (isManagedOssMedia(media)) {
            return requireStorage("oss");
        }
        return resolveCurrentStorage();
    }

    private boolean isExternalMedia(Media media) {
        if (media == null || !StringUtils.hasText(media.getFileUrl())) {
            return false;
        }
        String fileUrl = media.getFileUrl().trim();
        return (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) && !isManagedOssMedia(media);
    }

    private boolean isManagedOssMedia(Media media) {
        if (media == null || !StringUtils.hasText(media.getFileUrl())) {
            return false;
        }
        String fileUrl = media.getFileUrl().trim();
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            return false;
        }
        SystemConfig systemConfig = dyxAdminService.getSystemConfig();
        String publicUrlPrefix = normalizeOssUrlPrefix(systemConfig == null ? null : systemConfig.getOssPublicUrlPrefix());
        if (StringUtils.hasText(publicUrlPrefix) && fileUrl.startsWith(publicUrlPrefix)) {
            return true;
        }
        String defaultPublicUrlPrefix = buildDefaultOssPublicUrlPrefix(systemConfig);
        if (!StringUtils.hasText(defaultPublicUrlPrefix)) {
            return false;
        }
        if (fileUrl.startsWith(defaultPublicUrlPrefix)) {
            return true;
        }
        String httpPrefix = defaultPublicUrlPrefix.replaceFirst("^https://", "http://");
        return fileUrl.startsWith(httpPrefix);
    }

    private String normalizeOssUrlPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.trim().replaceAll("/+$", "") + "/";
    }

    private String buildDefaultOssPublicUrlPrefix(SystemConfig systemConfig) {
        if (systemConfig == null) {
            return "";
        }
        String endpoint = systemConfig.getOssEndpoint();
        String bucketName = systemConfig.getOssBucketName();
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucketName)) {
            return "";
        }
        String normalizedEndpoint = endpoint.trim();
        if (!normalizedEndpoint.startsWith("http://") && !normalizedEndpoint.startsWith("https://")) {
            normalizedEndpoint = "https://" + normalizedEndpoint;
        }
        String host = normalizedEndpoint.replaceFirst("^https?://", "").replaceAll("/.*$", "");
        return "https://" + bucketName.trim() + "." + host + "/";
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
