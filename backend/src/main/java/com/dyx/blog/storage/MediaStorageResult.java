package com.dyx.blog.storage;

/**
 * 媒体存储结果。
 */
public record MediaStorageResult(
        String fileName,
        String fileUrl
) {
}
