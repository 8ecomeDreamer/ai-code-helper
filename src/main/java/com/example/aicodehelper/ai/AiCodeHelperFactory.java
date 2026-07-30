package com.example.aicodehelper.ai;

import com.example.aicodehelper.ai.mcp.McpConfig;
import com.example.aicodehelper.ai.rag.RagConfig;
import com.example.aicodehelper.ai.tools.MathTools;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
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

    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构建AI服务

        return AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel) // 流式输出
                .chatMemory(chatMemory) // 会话记忆
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever) // Rag检索生成
                .tools(new MathTools()) // 工具调用
                .toolProvider(mcpToolProvider) // mcp工具调用
                .inputGuardrails(inputGuardrail)
                .build();
    }
}
