package com.example.aicodehelper.ai.prompt;

import lombok.Data;

/**
 * 系统提示词
 */
@Data
public class systemPrompt {
    /**
     * 文件路径
     */
    private String fromResource;
    /**
     * 文本
     */
    private String text;
}
