package com.example.aicodehelper.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeHelperServiceTest {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Test
    void chat() {
        String result = aiCodeHelperService.Chat("你好");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result = aiCodeHelperService.Chat("你好，我是程序员Jim");
        System.out.println(result);
        String result2 = aiCodeHelperService.Chat("你好，我是谁？");
        System.out.println(result2);
    }

    @Test
    void chatForRecord() {

        String userMessage = "你好，我是Jim，帮我制定学习报告";
        AiCodeHelperService.Record result = aiCodeHelperService.ChatForRecord(userMessage);
        System.out.println(result);
    }
}