package com.dyx.blog.common.interceptor;

import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.ClientIpUtil;
import com.dyx.blog.service.support.DistributedRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 公开接口限流拦截器。
 */
@Component
@RequiredArgsConstructor
public class PublicRateLimitInterceptor implements HandlerInterceptor {

    private final DistributedRateLimitService distributedRateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = ClientIpUtil.resolveClientIp(request);
        if (!distributedRateLimitService.allowGuestbook(clientIp)) {
            throw new BusinessException(429, "留言提交过于频繁，请稍后再试");
        }
        return true;
    }
}
