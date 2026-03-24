package com.dyx.blog.common.interceptor;

import com.dyx.blog.common.context.UserContext;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器。
 * 用于解析 Bearer Token 并校验当前后台请求是否已登录。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil dyxJwtUtil;

    /**
     * 在控制器执行前校验请求头中的 JWT。
     *
     * @param request 当前请求。
     * @param response 当前响应。
     * @param handler 处理器对象。
     * @return 是否继续执行。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("请先登录");
        }
        String token = authorization.substring(7);
        Long userId = dyxJwtUtil.parseUserId(token);
        UserContext.setUserId(userId);
        return true;
    }

    /**
     * 在请求完成后清理当前线程中的用户上下文。
     *
     * @param request 当前请求。
     * @param response 当前响应。
     * @param handler 处理器对象。
     * @param ex 异常对象。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
