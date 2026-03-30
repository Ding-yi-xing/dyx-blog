package com.dyx.blog.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Web 安全头部过滤器。
 * 为所有响应添加常见的安全头部，防止点击劫持、MIME 类型嗅探及提升传输安全性。
 */
@Component
@WebFilter("/*")
@Order(1)
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 防止点击劫持 (Clickjacking)
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

        // 2. 防止 MIME 类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 3. 启用浏览器 XSS 过滤器 (虽现代浏览器已逐渐弃用，但仍建议保留)
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 4. 内容安全策略 (CSP) - 限制资源加载来源，防止 XSS 和数据注入
        // 允许加载来自同源、百度/高德地图、阿里云 OSS 的资源
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://*.amap.com https://*.baidu.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "img-src 'self' data: https: blob:; " +
            "connect-src 'self' https://*.amap.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "frame-src 'self';");

        // 5. 严格传输安全 (HSTS) - 强制使用 HTTPS (生产环境建议开启)
        // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // 6. 引用策略
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        chain.doFilter(request, response);
    }
}
