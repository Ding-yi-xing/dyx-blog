package com.dyx.blog.config;

import com.dyx.blog.common.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${dyx.security.cors-allowed-origins:}")
    private List<String> dyxCorsAllowedOrigins;

    /**
     * 配置密码加密器。
     *
     * @return BCryptPasswordEncoder 实例。
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
        registry.addMapping("/**")
                .allowedOrigins(dyxCorsAllowedOrigins.toArray(String[]::new))
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
