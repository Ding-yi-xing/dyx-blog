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
import com.dyx.blog.entity.Work;
import com.dyx.blog.mapper.HonorMapper;
import com.dyx.blog.mapper.MediaMapper;
import com.dyx.blog.mapper.MomentMapper;
import com.dyx.blog.mapper.PostMapper;
import com.dyx.blog.mapper.ProfileMapper;
import com.dyx.blog.mapper.ProjectMapper;
import com.dyx.blog.mapper.WorkMapper;
import com.dyx.blog.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
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
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "pdf", "mp4", "webm", "mov", "m4v"
    );

    private final MediaMapper dyxMediaMapper;
    private final ProfileMapper dyxProfileMapper;
    private final PostMapper dyxPostMapper;
    private final ProjectMapper dyxProjectMapper;
    private final WorkMapper dyxWorkMapper;
    private final MomentMapper dyxMomentMapper;
    private final HonorMapper dyxHonorMapper;
    private final FileProperties dyxFileProperties;

    /**
     * 上传媒体文件。
     *
     * @param file 上传文件。
     * @return 保存后的媒体资源对象。
     */
    @Override
    public Media upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        try {
            Path uploadDirectory = getUploadDirectory();
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String storedFileName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
            Path targetPath = uploadDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media();
            media.setOriginalName(originalFilename);
            media.setFileName(storedFileName);
            media.setFileUrl(buildFileUrl(storedFileName));
            media.setMediaType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setCreatedAt(LocalDateTime.now());
            dyxMediaMapper.insert(media);
            return media;
        } catch (IOException exception) {
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 导入 uploads 目录下已存在的文件。
     *
     * @return 导入数量。
     */
    @Override
    public int importExistingFiles() {
        try {
            cleanupMissingMediaRecords();
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
                    media.setFileUrl(buildFileUrl(fileName));
                    media.setMediaType(Files.probeContentType(filePath));
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
        cleanupMissingMediaRecords();
        return dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                .orderByDesc(Media::getCreatedAt));
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
        try {
            Path filePath = resolveStoredFilePath(media);
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new BusinessException("删除媒体文件失败");
        }
        dyxMediaMapper.deleteById(id);
    }

    private Path getUploadDirectory() throws IOException {
        Path uploadDirectory = Paths.get(dyxFileProperties.getUploadPath()).toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        return uploadDirectory;
    }

    private String buildFileUrl(String fileName) {
        return dyxFileProperties.getAccessPrefix() + fileName;
    }

    private Path resolveStoredFilePath(Media media) throws IOException {
        Path uploadDirectory = getUploadDirectory();
        if (StringUtils.hasText(media.getFileName())) {
            return uploadDirectory.resolve(media.getFileName()).normalize();
        }
        String normalizedPrefix = normalizeAccessPrefix();
        String fileUrl = media.getFileUrl();
        String relativePath = fileUrl.startsWith(normalizedPrefix) ? fileUrl.substring(normalizedPrefix.length()) : fileUrl;
        return uploadDirectory.resolve(relativePath).normalize();
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
        try {
            return Files.exists(resolveStoredFilePath(media));
        } catch (IOException exception) {
            return false;
        }
    }

    private String normalizeAccessPrefix() {
        String accessPrefix = dyxFileProperties.getAccessPrefix();
        if (!StringUtils.hasText(accessPrefix)) {
            return "/";
        }
        return accessPrefix.endsWith("/") ? accessPrefix : accessPrefix + "/";
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
