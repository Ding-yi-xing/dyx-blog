package com.dyx.blog.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果对象。
 *
 * @param <T> 实际业务数据类型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码。 */
    private Integer code;

    /** 响应消息。 */
    private String message;

    /** 业务数据。 */
    private T data;

    /**
     * 返回成功结果。
     *
     * @param data 返回数据。
     * @param <T> 数据类型。
     * @return 成功响应对象。
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 返回成功结果。
     *
     * @return 成功响应对象。
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 返回失败结果。
     *
     * @param message 失败消息。
     * @return 失败响应对象。
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 返回指定状态码的失败结果。
     *
     * @param code 状态码。
     * @param message 失败消息。
     * @return 失败响应对象。
     */
    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
