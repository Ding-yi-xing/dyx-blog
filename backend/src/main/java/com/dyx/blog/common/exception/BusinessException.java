package com.dyx.blog.common.exception;

/**
 * 业务异常。
 * 用于封装可预期的业务错误并统一交由全局异常处理器返回。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 创建业务异常。
     *
     * @param message 异常消息。
     */
    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 创建带状态码的业务异常。
     *
     * @param code 状态码。
     * @param message 异常消息。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
