package com.dyx.blog.common.exception;

/**
 * 业务异常。
 * 用于封装可预期的业务错误并统一交由全局异常处理器返回。
 */
public class BusinessException extends RuntimeException {

    /**
     * 创建业务异常。
     *
     * @param message 异常消息。
     */
    public BusinessException(String message) {
        super(message);
    }
}
