package com.dyx.blog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 荣誉实体。
 * 保存关于我页面与后台荣誉管理中使用的奖项、证书与图集信息。
 */
@Data
@TableName("dyx_honor")
public class Honor {

    /** 荣誉主键，序列化为字符串以避免前端精度丢失。 */
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 荣誉标题。 */
    private String title;

    /** 颁发机构。 */
    private String issuer;

    /** 荣誉描述。 */
    private String description;

    /** 荣誉封面图。 */
    private String coverImage;

    /** 荣誉图集地址集合。 */
    private String imageUrls;

    /** 证书或附件文件地址。 */
    private String attachmentUrl;

    /** 获奖时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime awardAt;

    /** 排序值，值越小越靠前。 */
    private Integer sortOrder;

    /** 发布状态，1 表示前台可见。 */
    private Integer published;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
