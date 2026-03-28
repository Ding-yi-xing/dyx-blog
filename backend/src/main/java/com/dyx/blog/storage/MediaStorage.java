package com.dyx.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体文件存储接口。
 */
public interface MediaStorage {

    /**
     * 存储类型。
     */
    String getStorageType();

    /**
     * 上传文件。
     */
    MediaStorageResult upload(MultipartFile file, String storedFileName);

    /**
     * 删除文件。
     */
    void delete(String fileName, String fileUrl);

    /**
     * 判断文件是否存在。
     */
    boolean exists(String fileName, String fileUrl);
}
