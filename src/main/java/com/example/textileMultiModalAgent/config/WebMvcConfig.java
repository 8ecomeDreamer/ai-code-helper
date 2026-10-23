package com.example.textileMultiModalAgent.config;

import com.example.textileMultiModalAgent.common.security.ApiCallLogInterceptor;
import com.example.textileMultiModalAgent.common.security.JwtAuthInterceptor;
import com.example.textileMultiModalAgent.common.security.OptionalAuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册 JWT 认证拦截器、/ai/** 可选鉴权拦截器与模块 API 调用统计拦截器
 * 放行登录接口（/ai/** 保持公开，但登录用户记名：令牌合法才装载上下文，匿名亦可用）
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Resource
    private OptionalAuthInterceptor optionalAuthInterceptor;

    @Resource
    private ApiCallLogInterceptor apiCallLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器：除登录/公开 AI 入口外全域强制鉴权
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/ai/**", "/error");
        // 可选鉴权：/ai/** 带合法令牌（头或 ?token=）时装载登录用户上下文，匿名不拒绝
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns("/ai/**");
        // 调用统计拦截器：仅覆盖 AI 业务入口，落 ai_business_log(log_type=api_call)
        registry.addInterceptor(apiCallLogInterceptor)
                .addPathPatterns("/ai/chat", "/ai/asr", "/quote/**", "/research/**",
                        "/compliance/**", "/fabric/**", "/erp/**");
    }
}
