package com.example.aicodehelper.ai.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.*;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ProfanityGuardrail implements InputGuardrail {

    private static final List<String> PROFANITY_WORDS = Arrays.asList(
        "fuck", "shit", "bitch", "asshole", "damn",
        "傻逼", "傻B", "SB", "他妈的", "草泥马"
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        try {
            // 使用 singleText() 方法获取文本内容
            String text = userMessage.singleText();

            // 检查文本是否为空
            if (text == null || text.trim().isEmpty()) {
                // 空消息直接通过
                return this.success();
            }

            // 将文本转为小写以便不区分大小写匹配
            String lowerText = text.toLowerCase();

            // 检查是否包含任何脏话
            for (String profanity : PROFANITY_WORDS) {
                if (lowerText.contains(profanity.toLowerCase())) {
                    // 发现脏话，返回失败结果
                    return this.failure("检测到不文明用语：" + profanity);
                }
            }

            // 没有发现脏话，通过检查
            return this.success();

        } catch (Exception e) {
            // 如果 singleText() 抛出异常（比如内容不是单一文本）
            // 可以处理其他类型的内容，或者直接返回失败
            return this.failure("无法处理非文本内容或内容格式错误");
        }
    }
}

