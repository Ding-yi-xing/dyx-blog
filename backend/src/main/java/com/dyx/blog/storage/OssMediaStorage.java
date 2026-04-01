package com.dyx.blog.storage;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.DeleteObjectRequest;
import com.aliyun.sdk.service.oss2.models.ListObjectsV2Request;
import com.aliyun.sdk.service.oss2.models.ListObjectsV2Result;
import com.aliyun.sdk.service.oss2.models.ObjectSummary;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.entity.SystemConfig;
import com.dyx.blog.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云 OSS 媒体存储实现。
 * 负责根据系统配置与环境变量凭证完成对象上传、删除、存在性检查与对象列表读取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssMediaStorage implements MediaStorage {

    /** 环境变量中的 OSS Access Key ID 键名。 */
    private static final String OSS_ACCESS_KEY_ID = "OSS_ACCESS_KEY_ID";

    /** 环境变量中的 OSS Access Key Secret 键名。 */
    private static final String OSS_ACCESS_KEY_SECRET = "OSS_ACCESS_KEY_SECRET";

    private final AdminService dyxAdminService;

    @Override
    public String getStorageType() {
        return "oss";
    }

    /**
     * 上传文件到 OSS，并返回对象键与公网访问地址。
     * 对象键会基于系统配置中的基础目录与业务层传入的文件名拼接生成。
     */
    @Override
    public MediaStorageResult upload(MultipartFile file, String storedFileName) {
        SystemConfig systemConfig = requireOssConfig();
        String objectKey = buildObjectKey(systemConfig, storedFileName);
        OSSClient ossClient = null;
        try {
            ossClient = buildClient(systemConfig);
            PutObjectRequest request = PutObjectRequest.newBuilder()
                    .bucket(systemConfig.getOssBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(Math.toIntExact(file.getSize()))
                    .body(BinaryData.fromStream(file.getInputStream(), file.getSize()))
                    .build();
            ossClient.putObject(request);
            return new MediaStorageResult(objectKey, buildPublicUrl(systemConfig, objectKey));
        } catch (IOException exception) {
            log.error("上传 OSS 文件流失败，bucket={}, endpoint={}, region={}, objectKey={}",
                    systemConfig.getOssBucketName(), normalizeEndpointHost(systemConfig.getOssEndpoint()), resolveRegion(systemConfig), objectKey, exception);
            throw new BusinessException("OSS 文件上传失败：读取上传文件流异常");
        } catch (RuntimeException exception) {
            log.error("上传 OSS 文件失败，bucket={}, endpoint={}, region={}, objectKey={}",
                    systemConfig.getOssBucketName(), normalizeEndpointHost(systemConfig.getOssEndpoint()), resolveRegion(systemConfig), objectKey, exception);
            throw new BusinessException("OSS 文件上传失败：" + resolveOssErrorMessage(exception));
        } finally {
            closeQuietly(ossClient);
        }
    }

    /**
     * 删除 OSS 中的目标对象。
     * 优先使用 fileName 作为对象键，fileUrl 仅在缺少 fileName 时用于反推对象键。
     */
    @Override
    public void delete(String fileName, String fileUrl) {
        SystemConfig systemConfig = requireOssConfig();
        String objectKey = resolveObjectKey(systemConfig, fileName, fileUrl);
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        OSSClient ossClient = null;
        try {
            ossClient = buildClient(systemConfig);
            DeleteObjectRequest request = DeleteObjectRequest.newBuilder()
                    .bucket(systemConfig.getOssBucketName())
                    .key(objectKey)
                    .build();
            ossClient.deleteObject(request);
        } catch (RuntimeException exception) {
            log.error("删除 OSS 文件失败，bucket={}, endpoint={}, region={}, objectKey={}",
                    systemConfig.getOssBucketName(), normalizeEndpointHost(systemConfig.getOssEndpoint()), resolveRegion(systemConfig), objectKey, exception);
            throw new BusinessException("删除 OSS 文件失败：" + resolveOssErrorMessage(exception));
        } finally {
            closeQuietly(ossClient);
        }
    }

    /**
     * 检查 OSS 对象是否存在。
     * 当 OSS 配置缺失或检查过程异常时，保守返回 true，避免业务层误判为可安全删除。
     */
    @Override
    public boolean exists(String fileName, String fileUrl) {
        SystemConfig systemConfig = dyxAdminService.getSystemConfig();
        if (systemConfig == null || !StringUtils.hasText(systemConfig.getOssBucketName()) || !StringUtils.hasText(systemConfig.getOssEndpoint())) {
            return true;
        }
        String objectKey = resolveObjectKey(systemConfig, fileName, fileUrl);
        if (!StringUtils.hasText(objectKey)) {
            return false;
        }
        OSSClient ossClient = null;
        try {
            ossClient = buildClient(systemConfig);
            return ossClient.doesObjectExist(systemConfig.getOssBucketName(), objectKey);
        } catch (RuntimeException exception) {
            log.warn("检查 OSS 文件存在性失败，bucket={}, endpoint={}, region={}, objectKey={}",
                    systemConfig.getOssBucketName(), normalizeEndpointHost(systemConfig.getOssEndpoint()), resolveRegion(systemConfig), objectKey, exception);
            return true;
        } finally {
            closeQuietly(ossClient);
        }
    }

    /**
     * 列出当前 Bucket 基础目录下的全部对象。
     * 返回结果会过滤目录占位对象，并补齐可访问 URL、推断内容类型与最后修改时间。
     */
    public List<StoredObject> listStoredObjects() {
        SystemConfig systemConfig = requireOssConfig();
        OSSClient ossClient = null;
        try {
            ossClient = buildClient(systemConfig);
            List<StoredObject> storedObjects = new ArrayList<>();
            String continuationToken = null;
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.newBuilder()
                        .bucket(systemConfig.getOssBucketName())
                        .maxKeys(200L);
                String prefix = normalizeBaseDir(systemConfig.getOssBaseDir());
                if (StringUtils.hasText(prefix)) {
                    requestBuilder.prefix(prefix);
                }
                if (StringUtils.hasText(continuationToken)) {
                    requestBuilder.continuationToken(continuationToken);
                }
                ListObjectsV2Result result = ossClient.listObjectsV2(requestBuilder.build());
                if (result.contents() != null) {
                    for (ObjectSummary objectSummary : result.contents()) {
                        String objectKey = objectSummary.key();
                        if (!StringUtils.hasText(objectKey) || objectKey.endsWith("/")) {
                            continue;
                        }
                        storedObjects.add(new StoredObject(
                                objectKey,
                                extractObjectName(objectKey),
                                buildPublicUrl(systemConfig, objectKey),
                                resolveContentType(objectKey),
                                objectSummary.size(),
                                toLocalDateTime(objectSummary.lastModified())
                        ));
                    }
                }
                continuationToken = result.nextContinuationToken();
                if (!Boolean.TRUE.equals(result.isTruncated())) {
                    break;
                }
            } while (StringUtils.hasText(continuationToken));
            return storedObjects;
        } catch (RuntimeException exception) {
            log.error("读取 OSS 文件列表失败，bucket={}, endpoint={}, region={}, prefix={}",
                    systemConfig.getOssBucketName(), normalizeEndpointHost(systemConfig.getOssEndpoint()), resolveRegion(systemConfig), normalizeBaseDir(systemConfig.getOssBaseDir()), exception);
            throw new BusinessException("读取 OSS 文件列表失败：" + resolveOssErrorMessage(exception));
        } finally {
            closeQuietly(ossClient);
        }
    }

    /**
     * 判断运行环境中是否已配置 OSS 访问凭证。
     */
    public static boolean hasConfiguredCredentials() {
        return StringUtils.hasText(readCredential(OSS_ACCESS_KEY_ID)) && StringUtils.hasText(readCredential(OSS_ACCESS_KEY_SECRET));
    }

    /**
     * 使用当前系统配置与环境变量凭证创建 OSS 客户端。
     */
    private OSSClient buildClient(SystemConfig systemConfig) {
        String accessKeyId = readCredential(OSS_ACCESS_KEY_ID);
        String accessKeySecret = readCredential(OSS_ACCESS_KEY_SECRET);
        if (!StringUtils.hasText(accessKeyId) || !StringUtils.hasText(accessKeySecret)) {
            throw new BusinessException("OSS 凭证未配置，请先在服务端环境变量中设置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET");
        }
        return OSSClient.newBuilder()
                .endpoint(normalizeEndpointHost(systemConfig.getOssEndpoint()))
                .region(resolveRegion(systemConfig))
                .credentialsProvider(new StaticCredentialsProvider(accessKeyId, accessKeySecret))
                .connectTimeout(Duration.ofSeconds(10))
                .readWriteTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 读取并校验 OSS 所需系统配置。
     */
    private SystemConfig requireOssConfig() {
        SystemConfig systemConfig = dyxAdminService.getSystemConfig();
        if (systemConfig == null) {
            throw new BusinessException("系统配置不存在");
        }
        if (!StringUtils.hasText(systemConfig.getOssEndpoint())) {
            throw new BusinessException("请先配置 OSS Endpoint");
        }
        if (!StringUtils.hasText(systemConfig.getOssBucketName())) {
            throw new BusinessException("请先配置 OSS Bucket");
        }
        if (!hasConfiguredCredentials()) {
            throw new BusinessException("OSS 凭证未配置，请先在服务端环境变量中设置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET");
        }
        return systemConfig;
    }

    /**
     * 推导 OSS 所属地域。
     * 优先使用显式配置，未配置时再尝试从 Endpoint 主机名中解析。
     */
    private String resolveRegion(SystemConfig systemConfig) {
        if (StringUtils.hasText(systemConfig.getOssRegion())) {
            return systemConfig.getOssRegion().trim();
        }
        String endpointHost = normalizeEndpointHost(systemConfig.getOssEndpoint());
        if (!StringUtils.hasText(endpointHost)) {
            return "cn-hangzhou";
        }
        String[] labels = endpointHost.split("\\.");
        if (labels.length == 0) {
            return "cn-hangzhou";
        }
        String firstLabel = labels[0];
        String bucketName = systemConfig.getOssBucketName();
        if (StringUtils.hasText(bucketName) && bucketName.equals(firstLabel) && labels.length > 1 && labels[1].startsWith("oss-")) {
            return labels[1].substring(4);
        }
        if (firstLabel.startsWith("oss-")) {
            return firstLabel.substring(4);
        }
        return firstLabel;
    }

    /**
     * 基于基础目录与存储文件名拼接对象键。
     */
    private String buildObjectKey(SystemConfig systemConfig, String storedFileName) {
        String baseDir = normalizeBaseDir(systemConfig.getOssBaseDir());
        return StringUtils.hasText(baseDir) ? baseDir + storedFileName : storedFileName;
    }

    /**
     * 生成对象的公网访问地址。
     * 优先使用显式配置的访问前缀，未配置时按 Bucket 与 Endpoint 默认拼接。
     */
    private String buildPublicUrl(SystemConfig systemConfig, String objectKey) {
        String publicUrlPrefix = normalizeUrlPrefix(systemConfig.getOssPublicUrlPrefix());
        if (StringUtils.hasText(publicUrlPrefix)) {
            return publicUrlPrefix + objectKey;
        }
        return buildDefaultPublicUrlPrefix(systemConfig) + objectKey;
    }

    /**
     * 根据文件名或访问地址解析 OSS 对象键。
     */
    private String resolveObjectKey(SystemConfig systemConfig, String fileName, String fileUrl) {
        if (StringUtils.hasText(fileName)) {
            return fileName.trim();
        }
        if (!StringUtils.hasText(fileUrl)) {
            return null;
        }
        String publicUrlPrefix = normalizeUrlPrefix(systemConfig.getOssPublicUrlPrefix());
        if (StringUtils.hasText(publicUrlPrefix) && fileUrl.startsWith(publicUrlPrefix)) {
            return fileUrl.substring(publicUrlPrefix.length());
        }
        String defaultPublicUrlPrefix = buildDefaultPublicUrlPrefix(systemConfig);
        if (fileUrl.startsWith(defaultPublicUrlPrefix)) {
            return fileUrl.substring(defaultPublicUrlPrefix.length());
        }
        String httpPrefix = defaultPublicUrlPrefix.replaceFirst("^https://", "http://");
        if (fileUrl.startsWith(httpPrefix)) {
            return fileUrl.substring(httpPrefix.length());
        }
        return fileUrl;
    }

    /**
     * 规范化基础目录，统一为不带前导斜杠、带结尾斜杠的形式。
     */
    private String normalizeBaseDir(String baseDir) {
        if (!StringUtils.hasText(baseDir)) {
            return "";
        }
        String normalized = baseDir.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.isEmpty() ? "" : (normalized.endsWith("/") ? normalized : normalized + "/");
    }

    /**
     * 规范化公网访问前缀，统一补齐单个结尾斜杠。
     */
    private String normalizeUrlPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.trim().replaceAll("/+$", "") + "/";
    }

    /**
     * 提取 Endpoint 主机名，去掉协议头与多余路径。
     */
    private String normalizeEndpointHost(String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return "";
        }
        String normalized = endpoint.trim().replaceFirst("^https?://", "").replaceAll("/+$", "");
        int slashIndex = normalized.indexOf('/');
        return slashIndex >= 0 ? normalized.substring(0, slashIndex) : normalized;
    }

    /**
     * 按 Bucket 与 Endpoint 生成默认公网访问前缀。
     */
    private String buildDefaultPublicUrlPrefix(SystemConfig systemConfig) {
        String endpointHost = normalizeEndpointHost(systemConfig.getOssEndpoint());
        String bucketName = systemConfig.getOssBucketName().trim();
        String host = endpointHost.startsWith(bucketName + ".") ? endpointHost : bucketName + "." + endpointHost;
        return "https://" + host + "/";
    }

    /**
     * 从对象键中提取最终文件名。
     */
    private String extractObjectName(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return "";
        }
        int separatorIndex = objectKey.lastIndexOf('/');
        return separatorIndex >= 0 ? objectKey.substring(separatorIndex + 1) : objectKey;
    }

    /**
     * 根据对象键猜测内容类型，失败时回退到通用二进制流类型。
     */
    private String resolveContentType(String objectKey) {
        String contentType = URLConnection.guessContentTypeFromName(objectKey);
        return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
    }

    /**
     * 将 OSS 返回的 Instant 转换为系统默认时区的 LocalDateTime。
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * 尽量提取可读的 OSS 异常消息，便于返回前端或记录日志。
     */
    private String resolveOssErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "未知错误";
        }
        if (StringUtils.hasText(throwable.getMessage())) {
            return throwable.getMessage();
        }
        Throwable cause = throwable.getCause();
        if (cause != null && StringUtils.hasText(cause.getMessage())) {
            return cause.getMessage();
        }
        return throwable.getClass().getSimpleName();
    }

    /**
     * 读取并裁剪环境变量凭证值。
     */
    private static String readCredential(String key) {
        String value = System.getenv(key);
        return value == null ? null : value.trim();
    }

    /**
     * 安静关闭 OSS 客户端，避免清理阶段掩盖主异常。
     */
    private void closeQuietly(OSSClient ossClient) {
        if (ossClient == null) {
            return;
        }
        try {
            ossClient.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * OSS 已存储对象摘要。
     *
     * @param objectKey      OSS 对象键。
     * @param originalName   从对象键提取的文件名。
     * @param fileUrl        对外访问地址。
     * @param contentType    推断出的内容类型。
     * @param fileSize       文件大小，单位字节。
     * @param lastModifiedAt 最后修改时间。
     */
    public record StoredObject(
            String objectKey,
            String originalName,
            String fileUrl,
            String contentType,
            Long fileSize,
            LocalDateTime lastModifiedAt
    ) {
    }
}
