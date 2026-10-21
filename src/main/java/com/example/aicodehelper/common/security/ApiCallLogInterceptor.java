package com.example.aicodehelper.common.security;

import com.example.aicodehelper.service.admin.CallStatisticsService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 模块 API 调用统计拦截器：preHandle 记起始时间，afterCompletion 按路径前缀识别业务模块，
 * 将调用量/状态码/耗时/会话/用户异步落 ai_business_log（log_type=api_call）。
 *
 * <p>注册范围见 WebMvcConfig；未命中模块映射的路径直接跳过，不影响其余请求。</p>
 */
@Component
public class ApiCallLogInterceptor implements HandlerInterceptor {

    /** 请求属性键：进入时间戳（request attribute 跨异步派发保持，SSE 场景耗时为整轮流时长） */
    private static final String ATTR_START = "apiCallStartTs";

    @Resource
    private CallStatisticsService callStatisticsService;

    @Resource
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String module = moduleOf(request.getRequestURI());
        if (module == null) {
            return;
        }
        Object start = request.getAttribute(ATTR_START);
        long cost = start instanceof Long ? System.currentTimeMillis() - (Long) start : 0L;
        callStatisticsService.recordApiCall(module, response.getStatus(), cost,
                request.getMethod() + " " + request.getRequestURI(),
                resolveSessionId(request), resolveUserId(request));
    }

    /** 会话id：综合问答以 memoryId 传递（GET query / POST form 均可 getParameter），兼容 sessionId 命名 */
    private Long resolveSessionId(HttpServletRequest request) {
        String raw = request.getParameter("memoryId");
        if (raw == null || raw.isBlank()) {
            raw = request.getParameter("sessionId");
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 归属用户id：/ai/** 免鉴权，令牌合法才解析（POST 走 Authorization 头，GET SSE 走 ?token=），匿名返回 null */
    private Long resolveUserId(HttpServletRequest request) {
        try {
            return tokenService.getUserIdFromRequest(request);
        } catch (Exception e) {
            return null;
        }
    }

    /** URI 前缀 → 模块名（context-path=/api 也在 URI 里，用 contains 匹配业务段） */
    private String moduleOf(String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.contains("/ai/chat")) {
            return "综合问答";
        }
        if (uri.contains("/ai/asr")) {
            return "语音识别";
        }
        if (uri.contains("/quote")) {
            return "智能报价";
        }
        if (uri.contains("/research")) {
            return "竞品调研";
        }
        if (uri.contains("/compliance")) {
            return "合规校验";
        }
        if (uri.contains("/fabric")) {
            return "面料识别";
        }
        if (uri.contains("/erp")) {
            return "ERP数据";
        }
        return null;
    }
}
