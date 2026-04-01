package com.dyx.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 媒体文件存储接口。
 * 抽象本地存储与 OSS 存储的统一能力，供服务层按存储模式切换实现。
 */
public interface MediaStorage {

    /**
     * 返回当前存储实现的类型标识。
     * 约定值需与系统配置中的 storageType 保持一致，例如 local 或 oss。
     */
    String getStorageType();

    /**
     * 上传文件并返回统一的存储结果。
     *
     * @param file           待上传的原始文件流。
     * @param storedFileName 业务层生成的落库存储名，通常已完成去重处理。
     * @return 包含实际存储文件名与访问地址的结果对象。
     */
    MediaStorageResult upload(MultipartFile file, String storedFileName);

    /**
     * 按文件名或访问地址删除已存储文件。
     * 实现可根据各自存储介质优先解析 fileName，fileUrl 作为补充兜底信息。
     *
     * @param fileName 存储后的文件名或对象键。
     * @param fileUrl  对外访问地址。
     */
    void delete(String fileName, String fileUrl);

    /**
     * 判断目标文件是否存在。
     * 当底层存储不可用时，实现可以选择保守返回 true，避免误判导致业务误删。
     *
     * @param fileName 存储后的文件名或对象键。
     * @param fileUrl  对外访问地址。
     * @return true 表示文件存在或暂时无法安全确认不存在。
     */
    boolean exists(String fileName, String fileUrl);
}
