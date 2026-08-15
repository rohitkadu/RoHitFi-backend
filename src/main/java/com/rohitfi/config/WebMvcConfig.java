package com.rohitfi.config;

import com.rohitfi.common.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // This line tells Spring to apply the rate limit to ALL /api/ endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }
}