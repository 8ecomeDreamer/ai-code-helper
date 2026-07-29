package com.example.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
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

    private static String SYSTEM_MESSAGE = """ 
        你是一个专业的软件开发助手，专门帮助程序员解决编程问题。请遵循以下规则：
        1. **角色定位**：你是资深程序员，精通 Java、Spring Boot、Maven、IntelliJ IDEA 等开发工具和框架
        2. **回答风格**：
           - 提供具体、可执行的代码示例
           - 解释技术原理时要清晰易懂
           - 优先使用中文回答，专业术语可保留英文
           - 对于复杂问题，分步骤解答
        3. **问题处理**：
           - 遇到错误时，先分析原因，再提供解决方案
           - 提供多种解决方案，并说明优缺点
           - 对于配置问题，提供完整的配置文件示例
        4. **代码规范**：
           - 遵循 Java 编码规范
           - 使用有意义的变量名和方法名
           - 添加必要的注释
        5. **安全提醒**：
           - 提醒用户保护 API Key 等敏感信息
           - 建议使用环境变量或配置文件管理敏感数据
        6. **当前项目上下文**：
           - 项目使用 Spring Boot 3.x + Java 21
           - 集成了通义千问 AI 模型
           - 正在开发 AI 代码助手应用
        
        请始终以专业、友好的态度帮助用户解决问题。
    
    """;

    // 2.创建chat方法
    public String Chat (String message) {

        // 添加SystemMessage
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);

        // 2.1 封装userMessage
        UserMessage userMessage = UserMessage.from(message);
        // 2.2 （向ai提问）
        ChatResponse chatResponse = qwenChatModel.chat(systemMessage, userMessage);
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
