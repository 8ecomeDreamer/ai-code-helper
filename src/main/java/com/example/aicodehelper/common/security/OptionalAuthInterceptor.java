package com.example.aicodehelper.common.security;

import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.service.admin.SysLoginService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选鉴权拦截器：仅注册在 /ai/** 公开链路上。
 *
 * <p>与 {@link JwtAuthInterceptor} 的区别：令牌缺失或非法时<b>不拒绝</b>，
 * 只会在令牌有效时装载登录用户上下文，保证公开接口「登录则记名、匿名亦可用」。
 * 令牌来源兼容请求头与 token 查询参数（浏览器 EventSource 无法携带请求头）。</p>
 *
 * <p>线程上下文安全：preHandle 无有效用户时主动清理，防止线程池复用读到上一请求残留；
 * 异步（SSE）交接时同样清理，避免容器线程残留上下文。</p>
 */
@Component
public class OptionalAuthInterceptor implements HandlerInterceptor, AsyncHandlerInterceptor {

    @Resource
    private TokenService tokenService;

    @Resource
    private SysLoginService sysLoginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        Long userId = tokenService.getUserIdFromRequestOrParam(request);
        LoginUser loginUser = userId == null ? null : sysLoginService.loadLoginUser(userId);
        if (loginUser == null) {
            // 匿名访问或令牌失效：清理线程池可能残留的上一请求上下文，保持公开语义
            SecurityUtils.clear();
        } else {
            SecurityUtils.setLoginUser(loginUser);
        }
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                               Object handler) {
        // 异步（SSE）交接：容器线程即将释放，清理上下文防残留
        SecurityUtils.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityUtils.clear();
    }
}
