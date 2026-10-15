package com.example.aicodehelper.ai.app.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "aiCodeHelper", description = "langChain4j常用API")
public interface AiCodeHelperService {

/**
 * 这是一个AI对话接口，用于与用户进行交互式对话
 * 使用了系统消息注解来初始化AI的上下文和行为准则
 *
 * @fromResource 指定了系统提示信息的资源文件路径，该文件包含AI的初始设定和行为指导
 *
 * @param userMessage 用户输入的消息内容
 * @return AI对用户消息的响应
 */
    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    String Chat(String userMessage);

    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    Record ChatForRecord(String userMessage);

    // 学习报告
    record Record(String name, List<String> suggestionList){};

    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    Result<String> ChatWithRagResult(String userMessage);

    // 流式输出
    @SystemMessage(fromResource = "prompt/system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String message);
}
