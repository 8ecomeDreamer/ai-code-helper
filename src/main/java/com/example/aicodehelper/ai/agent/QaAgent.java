package com.example.aicodehelper.ai.agent;

import com.example.aicodehelper.ai.app.AiCodeHelperHigherService;
import com.example.aicodehelper.ai.app.AiCodeHelperService;
import com.example.aicodehelper.common.security.ModelCallContext;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * 综合问答 Agent：统一对外暴露文本与多模态两条问答链路。
 *
 * <p>人设 prompt 在 {@code prompt/system-prompt.txt}（AiServices 装配期注入），
 * 记忆/护栏/RAG/工具/MCP 的装配在 AiCodeHelperFactory；多模态 I/O 在
 * {@link AiCodeHelperHigherService}（DashScope 官方 SDK 流式）。本 Agent 作为
 * 问答能力的统一门面，控制器不再直接持有两条底层链路。</p>
 *
 * <p>会话/用户归属：多模态链路经显式参数落到 model_call 统计；文本 AiServices 链路无法
 * 透传参数，改用 {@link ModelCallContext} 在订阅线程写入、由 {@code ChatModelListener}
 * 的 onRequest 读取（{@code Flux.defer} 保证 set 与模型请求在同一订阅线程）。</p>
 */
@Slf4j
@Component
public class QaAgent extends BizAgent {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Resource
    private AiCodeHelperHigherService aiCodeHelperHigherService;

    /** 纯文本流式问答（AiServices 声明式链路：记忆 + 护栏 + RAG + 工具） */
    public Flux<String> chatStream(int memoryId, String message, Long userId) {
        return Flux.defer(() -> {
            ModelCallContext.set((long) memoryId, userId);
            return aiCodeHelperService.chatStream(memoryId, message);
        }).doFinally(signal -> ModelCallContext.clear());
    }

    /** 多模态流式问答：图片 dataUri + 文档附件（PDF 自动切 qwen3.8 系列） */
    public Flux<String> multiModelChat(List<String> dataUris, List<MultipartFile> files, String message,
                                       int memoryId, Long userId) {
        return aiCodeHelperHigherService.multiModelChat(dataUris, files, message, (long) memoryId, userId);
    }
}
