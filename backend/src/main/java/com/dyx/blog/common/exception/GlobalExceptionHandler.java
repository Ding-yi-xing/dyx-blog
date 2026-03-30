package com.dyx.blog.common.exception;

import com.dyx.blog.common.response.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 用于统一转换参数校验异常、业务异常和系统异常的返回结构。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常对象。
     * @return 统一失败响应。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Result.failure(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理请求体校验异常。
     *
     * @param exception 参数校验异常。
     * @return 统一失败响应。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() == null
                ? "请求参数不合法"
                : exception.getBindingResult().getFieldError().getDefaultMessage();
        return Result.failure(400, message);
    }

    /**
     * 处理表单绑定校验异常。
     *
     * @param exception 绑定异常。
     * @return 统一失败响应。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldError() == null
                ? "请求参数不合法"
                : exception.getBindingResult().getFieldError().getDefaultMessage();
        return Result.failure(400, message);
    }

    /**
     * 处理约束校验异常。
     *
     * @param exception 约束校验异常。
     * @return 统一失败响应。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return Result.failure(400, exception.getMessage());
    }

    /**
     * 处理系统兜底异常。
     *
     * @param exception 系统异常对象。
     * @return 统一失败响应。
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return Result.failure("系统繁忙，请稍后重试");
    }
}
