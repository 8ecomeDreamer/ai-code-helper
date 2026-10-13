package com.example.aicodehelper.ai.model;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 通义千问「视觉理解」模型配置（面料疵点检测 / 综合问答图片链路专用）。
 *
 * <p>文本模型（qwen-max）无法处理图片，视觉理解需要使用多模态模型（qwen-vl-max 等）。
 * 配置项：{@code textile.vision.model-name}（默认 qwen-vl-max）、
 * {@code textile.vision.api-key}（缺省复用文本模型 apiKey）。</p>
 *
 * <p>注意：yml 中 {@code langChain4j.community.dashscope.image-model} 是 starter 的
 * <b>图片生成</b>模型（QwenImageModel）配置，与图片理解无关，视觉链路不会读取它。</p>
 */
@Slf4j
@Configuration
@Data
public class QwenMultiModelConfig {

    /** 视觉模型名称，默认 qwen-vl-max，可通过配置覆盖 */
    @Value("${textile.vision.model-name:qwen-vl-max}")
    private String visionModelName;

    /** 视觉模型 apiKey，缺省复用 dashscope 文本模型的 apiKey（嵌套占位符取默认值） */
    @Value("${textile.vision.api-key:${langChain4j.community.dashscope.chat-model.api-key}}")
    private String apiKey;

    @Resource
    private ChatModelListener chatModelListener;

    @Bean
    public ChatModel qwenVisionChatModel() {
        log.info("初始化视觉理解 ChatModel：model={}", visionModelName);
        return QwenChatModel.builder()
                .modelName(visionModelName)
                .apiKey(apiKey)
                .isMultimodalModel(true) // 走多模态会话接口，支持 ImageContent
                .listeners(List.of(chatModelListener))
                .build();
    }

    /**
     * 视觉模型的流式版本：综合问答多模态链路（文字+图片）逐 token 输出。
     * isMultimodalModel(true) 让 dashscope 走多模态会话接口以支持 ImageContent。
     */
    @Bean
    public StreamingChatModel qwenVisionStreamingChatModel() {
        return QwenStreamingChatModel.builder()
                .modelName(visionModelName)
                .apiKey(apiKey)
                .isMultimodalModel(true)
                .listeners(List.of(chatModelListener))
                .build();
    }
}
