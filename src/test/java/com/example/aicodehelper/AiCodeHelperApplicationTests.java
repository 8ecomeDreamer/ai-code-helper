package com.example.aicodehelper;

import com.example.aicodehelper.ai.AiCodeHelper;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import  jakarta.annotation.Resource;

@SpringBootTest
class AiCodeHelperApplicationTests {

    @Resource
    private AiCodeHelper aiCodeHelper;

    @Test
    void contextLoads() {
    }

    @Test
    void chat() {
        System.out.println(aiCodeHelper.Chat("你好, 这里是程序员Jim"));
    }


    @Test
    void testChat() {
        // 1.封装UserMessage
        UserMessage userMessage = UserMessage.from(
            TextContent.from("给你一张表情包~"),
            ImageContent.from("https://c-ssl.dtstatic.com/uploads/blog/202303/24/20230324044542_2f62d.thumb.1000_0.jpeg")
        );
        // 2.调用chat方法
        aiCodeHelper.Chat(userMessage);
    }
}
