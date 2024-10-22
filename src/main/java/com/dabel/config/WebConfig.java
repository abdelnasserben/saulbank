package com.dabel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedUserModelAndRequestLoggingInterceptor authenticatedUserModelAndRequestLoggingInterceptor;

    public WebConfig(AuthenticatedUserModelAndRequestLoggingInterceptor authenticatedUserModelAndRequestLoggingInterceptor) {
        this.authenticatedUserModelAndRequestLoggingInterceptor = authenticatedUserModelAndRequestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticatedUserModelAndRequestLoggingInterceptor);
    }
}

