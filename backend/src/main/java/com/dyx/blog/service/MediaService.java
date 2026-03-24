package com.dyx.blog.service;

import com.dyx.blog.entity.Media;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 媒体资源服务接口。
 */
public interface MediaService {

    /**
     * 上传媒体文件并保存资源记录。
     *
     * @param file 上传文件。
     * @return 保存后的媒体资源对象。
     */
    Media upload(MultipartFile file);

    /**
     * 查询全部媒体资源。
     *
     * @return 媒体资源列表。
     */
    List<Media> listAll();
}
