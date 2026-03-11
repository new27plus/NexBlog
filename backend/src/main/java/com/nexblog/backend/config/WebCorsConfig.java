package com.nexblog.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /*
         * 为什么要配 CORS：
         * - 开发时前端通常跑在 http://localhost:5173
         * - 后端通常跑在 http://localhost:8080
         * - 浏览器认为这是“跨域请求”，默认会拦截
         *
         * 这段配置就是告诉后端：
         * - 允许来自前端开发地址的请求
         * - 允许常见 HTTP 方法（GET/POST/PUT/DELETE）
         * - 允许常见请求头
         *
         * 生产环境建议把 origin 改成正式域名，不要用 *
         */
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }
}
