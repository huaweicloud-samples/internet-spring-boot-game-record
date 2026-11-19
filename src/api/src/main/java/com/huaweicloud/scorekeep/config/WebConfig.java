package com.huaweicloud.scorekeep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口跨域策略。
 *
 * <p>页面与接口由同一进程同域提供，正常部署下不需要跨域放行。
 * 该配置用于本地分离调试：把前端跑在另一个端口时，
 * 通过环境变量 {@code CORS_ALLOWED_ORIGINS} 指定允许的来源（逗号分隔）。</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(@Value("${CORS_ALLOWED_ORIGINS:http://localhost:8080}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
