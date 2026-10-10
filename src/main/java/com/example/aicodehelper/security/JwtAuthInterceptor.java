package com.example.aicodehelper.security;

import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.service.admin.SysLoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器：校验令牌并装载当前登录用户上下文
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Resource
    private TokenService tokenService;

    @Resource
    private SysLoginService sysLoginService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        Long userId = tokenService.getUserIdFromRequest(request);
        LoginUser loginUser = userId == null ? null : sysLoginService.loadLoginUser(userId);
        if (loginUser == null) {
            writeUnauthorized(response);
            return false;
        }
        SecurityUtils.setLoginUser(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityUtils.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(Constants.UNAUTHORIZED, "登录状态已过期或访问未授权，请重新登录")));
    }
}
