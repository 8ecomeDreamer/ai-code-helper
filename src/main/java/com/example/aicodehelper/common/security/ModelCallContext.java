package com.example.aicodehelper.common.security;

/**
 * 模型调用上下文（请求线程内传递会话/用户归属）。
 *
 * <p>langchain4j 声明式模型（AiServices）的调用监听 {@code ChatModelListener} 无法直接拿到
 * 当前 HTTP 请求的用户/会话，故在控制器构建流式响应前把 {@code (sessionId, userId)} 放入本
 * ThreadLocal，监听 {@code onRequest} 读取并转存到 {@code attributes} map，
 * {@code onResponse/onError} 再据此落库归属。</p>
 *
 * <p>尽力而为：若模型请求在其它线程异步发起导致 ThreadLocal 不可见，则归属记为 null，
 * 不影响统计与主流程。多模态/面料/语音等链路改用显式参数传递，归属可靠。</p>
 */
public final class ModelCallContext {

    private static final ThreadLocal<long[]> HOLDER = new ThreadLocal<>();

    private ModelCallContext() {
    }

    /** 设置当前线程的会话与用户归属（null 以 -1 占位） */
    public static void set(Long sessionId, Long userId) {
        HOLDER.set(new long[]{sessionId == null ? -1L : sessionId, userId == null ? -1L : userId});
    }

    /** 读取当前线程会话id；无上下文返回 null */
    public static Long sessionId() {
        long[] v = HOLDER.get();
        if (v == null || v[0] < 0) {
            return null;
        }
        return v[0];
    }

    /** 读取当前线程用户id；无上下文返回 null */
    public static Long userId() {
        long[] v = HOLDER.get();
        if (v == null || v[1] < 0) {
            return null;
        }
        return v[1];
    }

    /** 清理，防线程池复用串味 */
    public static void clear() {
        HOLDER.remove();
    }
}
