package com.example.aicodehelper.ai.app;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;

import java.util.List;

@Tag(name = "aiCodeHelper", description = "AI助手")
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "com/example/aicodehelper/ai/prompt/system-prompt.txt")
    String Chat(String userMessage);

    @SystemMessage(fromResource = "com/example/aicodehelper/ai/prompt/system-prompt.txt")
    Record ChatForRecord(String userMessage);

    // 学习报告
    record Record(String name, List<String> suggestionList){};

    @SystemMessage(fromResource = "com/example/aicodehelper/ai/prompt/system-prompt.txt")
    Result<String> ChatWithRagResult(String userMessage);

    // 流式输出
    @SystemMessage(fromResource = "com/example/aicodehelper/ai/prompt/system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String message);
}
