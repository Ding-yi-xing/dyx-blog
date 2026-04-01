package com.dyx.blog.storage;

import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地媒体存储实现。
 * 负责将文件写入本地 uploads 目录，并通过路径校验防止越权访问目录外文件。
 */
@Component
@RequiredArgsConstructor
public class LocalMediaStorage implements MediaStorage {

    private final FileProperties dyxFileProperties;

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 将上传文件保存到本地目录。
     *
     * @param file           原始上传文件。
     * @param storedFileName 服务层生成的目标文件名。
     * @return 本地落盘后的文件名与访问地址。
     */
    @Override
    public MediaStorageResult upload(MultipartFile file, String storedFileName) {
        try {
            Path uploadDirectory = getUploadDirectory();
            Path targetPath = requireFileInUploadDirectory(uploadDirectory.resolve(storedFileName).normalize(), uploadDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new MediaStorageResult(storedFileName, buildFileUrl(storedFileName));
        } catch (IOException exception) {
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除本地媒体文件。
     *
     * @param fileName 存储后的文件名。
     * @param fileUrl  文件访问地址，作为 fileName 缺失时的兜底解析来源。
     */
    @Override
    public void delete(String fileName, String fileUrl) {
        try {
            Files.deleteIfExists(resolveStoredFilePath(fileName, fileUrl));
        } catch (IOException exception) {
            throw new BusinessException("删除媒体文件失败");
        }
    }

    /**
     * 检查本地文件是否存在。
     * 发生 IO 异常时保守返回 true，避免外层把暂时不可读的文件误判为不存在。
     */
    @Override
    public boolean exists(String fileName, String fileUrl) {
        try {
            return Files.exists(resolveStoredFilePath(fileName, fileUrl));
        } catch (IOException exception) {
            return true;
        }
    }

    /**
     * 获取上传根目录，不存在时自动创建。
     */
    private Path getUploadDirectory() throws IOException {
        Path uploadDirectory = Paths.get(dyxFileProperties.getUploadPath()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        return uploadDirectory;
    }

    /**
     * 按访问前缀拼接对外文件地址。
     */
    private String buildFileUrl(String fileName) {
        return normalizeAccessPrefix() + fileName;
    }

    /**
     * 根据文件名或访问地址解析本地绝对路径。
     */
    private Path resolveStoredFilePath(String fileName, String fileUrl) throws IOException {
        Path uploadDirectory = getUploadDirectory();
        Path resolvedPath;
        if (StringUtils.hasText(fileName)) {
            resolvedPath = uploadDirectory.resolve(fileName).normalize();
        } else {
            String normalizedPrefix = normalizeAccessPrefix();
            String relativePath = StringUtils.hasText(fileUrl) && fileUrl.startsWith(normalizedPrefix)
                    ? fileUrl.substring(normalizedPrefix.length())
                    : fileUrl;
            resolvedPath = uploadDirectory.resolve(relativePath).normalize();
        }
        return requireFileInUploadDirectory(resolvedPath, uploadDirectory);
    }

    /**
     * 确保目标路径仍位于上传目录内，防止目录穿越。
     */
    private Path requireFileInUploadDirectory(Path resolvedPath, Path uploadDirectory) {
        if (!resolvedPath.startsWith(uploadDirectory)) {
            throw new BusinessException("媒体资源路径无效");
        }
        return resolvedPath;
    }

    /**
     * 规范化访问前缀，统一补齐结尾斜杠。
     */
    private String normalizeAccessPrefix() {
        String accessPrefix = dyxFileProperties.getAccessPrefix();
        if (!StringUtils.hasText(accessPrefix)) {
            return "/";
        }
        return accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
    }
}
