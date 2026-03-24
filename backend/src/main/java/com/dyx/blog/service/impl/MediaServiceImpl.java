package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.config.FileProperties;
import com.dyx.blog.entity.Media;
import com.dyx.blog.mapper.MediaMapper;
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
import java.util.UUID;

/**
 * 媒体资源服务实现类。
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaMapper dyxMediaMapper;
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
            Path uploadDirectory = Paths.get(dyxFileProperties.getUploadPath()).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirectory);
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String storedFileName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
            Path targetPath = uploadDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Media media = new Media();
            media.setOriginalName(originalFilename);
            media.setFileName(storedFileName);
            media.setFileUrl(dyxFileProperties.getAccessPrefix() + storedFileName);
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
     * 查询全部媒体资源。
     *
     * @return 媒体资源列表。
     */
    @Override
    public List<Media> listAll() {
        return dyxMediaMapper.selectList(new LambdaQueryWrapper<Media>()
                .orderByDesc(Media::getCreatedAt));
    }
}
