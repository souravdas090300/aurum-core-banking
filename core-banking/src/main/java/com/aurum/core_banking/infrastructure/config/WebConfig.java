package com.aurum.core_banking.infrastructure.config;

import com.aurum.core_banking.infrastructure.security.RateLimitingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration — registers the rate-limiting interceptor for
 * the transfer API endpoints.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/v1/transfers/**");
    }
}
