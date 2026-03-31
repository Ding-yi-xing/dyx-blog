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
 */
@Component
@RequiredArgsConstructor
public class LocalMediaStorage implements MediaStorage {

    private final FileProperties dyxFileProperties;

    @Override
    public String getStorageType() {
        return "local";
    }

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

    @Override
    public void delete(String fileName, String fileUrl) {
        try {
            Files.deleteIfExists(resolveStoredFilePath(fileName, fileUrl));
        } catch (IOException exception) {
            throw new BusinessException("删除媒体文件失败");
        }
    }

    @Override
    public boolean exists(String fileName, String fileUrl) {
        try {
            return Files.exists(resolveStoredFilePath(fileName, fileUrl));
        } catch (IOException exception) {
            return true;
        }
    }

    private Path getUploadDirectory() throws IOException {
        Path uploadDirectory = Paths.get(dyxFileProperties.getUploadPath()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        return uploadDirectory;
    }

    private String buildFileUrl(String fileName) {
        return normalizeAccessPrefix() + fileName;
    }

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

    private Path requireFileInUploadDirectory(Path resolvedPath, Path uploadDirectory) {
        if (!resolvedPath.startsWith(uploadDirectory)) {
            throw new BusinessException("媒体资源路径无效");
        }
        return resolvedPath;
    }

    private String normalizeAccessPrefix() {
        String accessPrefix = dyxFileProperties.getAccessPrefix();
        if (!StringUtils.hasText(accessPrefix)) {
            return "/";
        }
        return accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
    }
}
