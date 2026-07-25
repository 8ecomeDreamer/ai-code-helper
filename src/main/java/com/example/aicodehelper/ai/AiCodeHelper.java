package com.example.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
@Slf4j
public class AiCodeHelper {

    // 1.引入chatModel
    @Resource
    private ChatModel qwenChatModel;

    // 2.创建chat方法
    public String Chat (String message) {

        // 2.1 封装userMessage
        UserMessage userMessage = UserMessage.from(message);
        // 2.2 （向ai提问）
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        // 2.3 （ai回答）
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("ai 回答：" + aiMessage.toString());
        // 2.4 （返回最终结果）
        return aiMessage.text();

    }

    public String Chat (UserMessage userMessage) {

        // 2.2 （向ai提问）
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        // 2.3 （ai回答）
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("ai 回答：" + aiMessage.toString());
        // 2.4 （返回最终结果）
        return aiMessage.text();
    }


}
