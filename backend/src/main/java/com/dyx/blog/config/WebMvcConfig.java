package com.dyx.blog.config;

import com.dyx.blog.common.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类。
 * 用于注册跨域、鉴权拦截器和媒体资源访问映射。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor dyxJwtAuthInterceptor;
    private final FileProperties dyxFileProperties;

    /**
     * 配置跨域规则。
     *
     * @param registry 跨域注册器。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 注册鉴权拦截器。
     *
     * @param registry 拦截器注册器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dyxJwtAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/auth/login", "/api/admin/media/content");
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
