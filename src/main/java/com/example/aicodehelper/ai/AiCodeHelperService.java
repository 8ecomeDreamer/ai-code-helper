package com.example.aicodehelper.ai;

import dev.langchain4j.service.SystemMessage;

import java.util.List;

public interface AiCodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String Chat(String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Record ChatForRecord(String userMessage);

    // 学习报告
    record Record(String name, List<String> suggestionList){};
}
