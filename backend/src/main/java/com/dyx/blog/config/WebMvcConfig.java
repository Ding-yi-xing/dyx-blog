package com.dyx.blog.config;

import com.dyx.blog.common.interceptor.JwtAuthInterceptor;
import com.dyx.blog.common.interceptor.PublicRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类。
 * 统一注册跨域规则、后台 JWT 拦截器以及本地媒体资源映射。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor dyxJwtAuthInterceptor;
    private final PublicRateLimitInterceptor publicRateLimitInterceptor;
    private final FileProperties dyxFileProperties;
    private final DyxSecurityProperties dyxSecurityProperties;

    /**
     * 注册系统使用的密码编码器。
     *
     * @return 基于 BCrypt 算法的密码编码器实例，用于登录校验与用户密码存储。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置跨域规则。
     * 允许来源列表来自安全配置属性，便于按环境统一调整。
     *
     * @param registry 跨域注册器。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> corsAllowedOrigins = dyxSecurityProperties.getCorsAllowedOrigins();
        registry.addMapping("/**")
                .allowedOriginPatterns(corsAllowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .allowCredentials(false);
    }

    /**
     * 注册后台接口的 JWT 鉴权拦截器。
     *
     * @param registry 拦截器注册器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dyxJwtAuthInterceptor)
                .addPathPatterns("/api/dyx-manager/**")
                .excludePathPatterns("/api/auth/login", "/api/dyx-manager/media/content");
        registry.addInterceptor(publicRateLimitInterceptor)
                .addPathPatterns("/api/site/guestbook/messages");
    }

    /**
     * 配置本地上传文件的静态资源访问映射。
     * 仅在本地存储模式下由文件访问前缀映射到上传目录。
     *
     * @param registry 资源处理器注册器。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(dyxFileProperties.getAccessPrefix() + "**")
                .addResourceLocations("file:" + dyxFileProperties.getUploadPath());
    }
}
