package com.example.aicodehelper.ai.app;

import com.example.aicodehelper.ai.mcp.McpConfig;
//import com.example.aicodehelper.ai.model.TextOnlyStreamingChatModel;
import com.example.aicodehelper.ai.tools.MathTools;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperFactory
{

    @Resource
    private ChatModel myQwenChatModel; // 这个chatModel携带了监听器，相当于仿制了一个chatModel
//    private ChatModel qwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;
    @Resource
    private McpConfig mcpConfig;
    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private InputGuardrail inputGuardrail;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private ChatMemoryStore chatMemoryStore;

    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 构建AI服务
        return AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                // 文本模型回放共享记忆时剥离图片等多模态内容，避免 qwen-max 报错
//                .streamingChatModel(chatMemoryStore) // 流式输出
//                // 与多模态链路共享同一份按 memoryId 隔离的会话记忆
//                .chatMemoryProvider(chatMemoryStore) // 每个会话独立存储
                .contentRetriever(contentRetriever) // Rag检索生成
                .tools(new MathTools()) // 工具调用
//                .toolProvider(mcpToolProvider) // mcp工具调用
                .inputGuardrails(inputGuardrail)
                .build();
    }
}



