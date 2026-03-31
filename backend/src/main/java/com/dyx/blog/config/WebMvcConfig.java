package com.dyx.blog.config;

import com.dyx.blog.common.interceptor.JwtAuthInterceptor;
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
 * 用于注册跨域、鉴权拦截器和媒体资源访问映射。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor dyxJwtAuthInterceptor;
    private final FileProperties dyxFileProperties;
    private final DyxSecurityProperties dyxSecurityProperties;

    /**
     * 注册系统使用的密码编码器。
     *
     * @return 基于 BCrypt 算法的密码编码器实例，用于登录校验与用户密码存储。
     * @throws IllegalArgumentException 当前实现不会主动抛出该异常；若 Spring 容器初始化参数异常，则由框架启动流程抛出。
     * @author Dyx
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置跨域规则。
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
     * 注册鉴权拦截器。
     *
     * @param registry 拦截器注册器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dyxJwtAuthInterceptor)
                .addPathPatterns("/api/dyx-manager/**")
                .excludePathPatterns("/api/auth/login", "/api/dyx-manager/media/content");
    }

    /**
     * 配置本地上传文件的静态资源访问映射。
     *
     * @param registry 资源处理器注册器。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(dyxFileProperties.getAccessPrefix() + "**")
                .addResourceLocations("file:" + dyxFileProperties.getUploadPath());
    }
}
