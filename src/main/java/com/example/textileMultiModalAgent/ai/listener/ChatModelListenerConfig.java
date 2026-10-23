package com.example.textileMultiModalAgent.ai.listener;


import com.example.textileMultiModalAgent.common.security.ModelCallContext;
import com.example.textileMultiModalAgent.service.admin.CallStatisticsService;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * langchain4j 声明式模型（QwenChatModel）调用监听：
 * 在原日志监听基础上，把每次模型调用（模型名/耗时/token 用量/会话/用户）异步落 ai_business_log，
 * 供后台 Dashboard「百炼接口使用情况」聚合与按用户限额。统计服务经 ObjectProvider 惰性获取，缺失时静默降级。
 *
 * <p>会话/用户归属经 {@link ModelCallContext} 在 onRequest 读取并转存到 attributes map，
 * onResponse/onError 再从 map 取回（跨线程安全，不依赖 ThreadLocal 在回调线程仍可见）。</p>
 */
@Configuration
public class ChatModelListenerConfig {

    /** attributes 共享 map 的计时键（request/response/error 上下文共用同一 map） */
    private static final String ATTR_START = "modelCallStartTs";
    /** attributes 共享 map 的会话归属键 */
    private static final String ATTR_SESSION = "modelCallSessionId";
    /** attributes 共享 map 的用户归属键 */
    private static final String ATTR_USER = "modelCallUserId";

    /** 声明式链路的统一场景名（多模块共用同一 ChatModel bean，模块区分由 api_call 日志承担） */
    private static final String SCENE = "文本对话模型";

    @Bean
    ChatModelListener chatModelListener(ObjectProvider<CallStatisticsService> statsProvider) {
        return new ChatModelListener() {

            private static final Logger log = LoggerFactory.getLogger(ChatModelListener.class);

            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("onRequest(): {}", requestContext.chatRequest());
                requestContext.attributes().put(ATTR_START, System.currentTimeMillis());
                // 捕获当前请求线程的会话/用户归属，转存进随请求走的 attributes，
                // 避免 onResponse 在模型 HTTP 回调线程上 ThreadLocal 已不可见
                putIfPresent(requestContext.attributes(), ATTR_SESSION, ModelCallContext.sessionId());
                putIfPresent(requestContext.attributes(), ATTR_USER, ModelCallContext.userId());
            }

            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
                CallStatisticsService stats = statsProvider.getIfAvailable();
                if (stats == null) {
                    return;
                }
                try {
                    String model = resolveModel(responseContext.chatResponse().modelName(),
                            responseContext.chatRequest().modelName());
                    TokenUsage usage = responseContext.chatResponse().tokenUsage();
                    stats.recordModelCall(SCENE, model, costOf(responseContext.attributes()),
                            usage == null ? null : usage.inputTokenCount(),
                            usage == null ? null : usage.outputTokenCount(), true,
                            asLong(responseContext.attributes().get(ATTR_SESSION)),
                            asLong(responseContext.attributes().get(ATTR_USER)));
                } catch (Exception e) {
                    log.warn("记录模型调用统计失败", e);
                }
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
                CallStatisticsService stats = statsProvider.getIfAvailable();
                if (stats == null) {
                    return;
                }
                try {
                    stats.recordModelCall(SCENE, resolveModel(errorContext.chatRequest().modelName()),
                            costOf(errorContext.attributes()), null, null, false,
                            asLong(errorContext.attributes().get(ATTR_SESSION)),
                            asLong(errorContext.attributes().get(ATTR_USER)));
                } catch (Exception e) {
                    log.warn("记录模型调用失败统计出错", e);
                }
            }

            private void putIfPresent(java.util.Map<Object, Object> attributes, String key, Long value) {
                if (value != null) {
                    attributes.put(key, value);
                }
            }

            private Long asLong(Object value) {
                return value instanceof Long ? (Long) value : null;
            }

            private String resolveModel(String... candidates) {
                for (String model : candidates) {
                    if (model != null && !model.isBlank()) {
                        return model;
                    }
                }
                return "未知模型";
            }

            private long costOf(java.util.Map<Object, Object> attributes) {
                Object start = attributes.get(ATTR_START);
                return start instanceof Long ? System.currentTimeMillis() - (Long) start : 0L;
            }
        };
    }
}
