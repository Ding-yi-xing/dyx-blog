package com.dyx.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果对象。
 *
 * @param <T> 列表项数据类型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 数据列表。 */
    private List<T> records;

    /** 总条数。 */
    private long total;

    /** 当前页码。 */
    private int page;

    /** 每页条数。 */
    private int pageSize;
}
