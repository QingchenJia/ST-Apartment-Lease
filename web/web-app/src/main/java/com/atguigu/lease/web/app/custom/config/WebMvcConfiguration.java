package com.atguigu.lease.web.app.custom.config;

import com.atguigu.lease.web.app.custom.converter.StringToBaseEnumConverterFactory;
import com.atguigu.lease.web.app.custom.interceptor.AuthenticationInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    @Resource
    private AuthenticationInterceptor authenticationInterceptor;

    @Resource
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authenticationInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns("/app/login/**");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(stringToBaseEnumConverterFactory);
    }
}