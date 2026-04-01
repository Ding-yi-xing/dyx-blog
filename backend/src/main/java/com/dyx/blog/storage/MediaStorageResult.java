package com.dyx.blog.storage;

/**
 * 媒体存储结果。
 * 封装存储实现返回的文件名与访问地址，供服务层统一落库。
 */
public record MediaStorageResult(
        /** 实际存储后的文件名。 */
        String fileName,
        /** 对外访问地址。 */
        String fileUrl
) {
}
