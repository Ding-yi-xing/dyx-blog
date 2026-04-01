package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 媒体资源实体。
 * 用于持久化媒体库中的文件元信息，供后台管理与前台资源引用复用。
 */
@Data
@TableName("dyx_media")
public class Media {

    /** 媒体主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 文件原始名称，便于后台展示用户上传时的文件名。 */
    private String originalName;

    /** 存储后的文件名，通常为去重后的实际落盘名称。 */
    private String fileName;

    /** 对外访问地址，供页面预览与业务字段引用。 */
    private String fileUrl;

    /** 媒体类型，如 image、pdf 等。 */
    private String mediaType;

    /** 文件大小，单位为字节。 */
    private Long fileSize;

    /** 资源入库时间。 */
    private LocalDateTime createdAt;
}
