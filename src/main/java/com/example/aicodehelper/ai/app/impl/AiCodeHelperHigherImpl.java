package com.example.aicodehelper.ai.app.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.example.aicodehelper.ai.app.service.AiCodeHelperHigherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模态流式问答的 DashScope 官方 SDK 实现（对应百炼「图像与视频理解」Java 示例）。
 *
 * <p>langchain4j 1.x 只有 {@code UserMessage + ImageContent} 的低阶支持，缺少多模态
 * 流式的声明式高阶 API，因此这里直接封装官方 SDK：拼装多模态 content（N 张图 + 1 段文字）
 * → {@code MultiModalConversation.streamCall} 增量流式调用 → RxJava Flowable 桥接为
 * {@link Flux} 逐增量文本下发。</p>
 *
 * <p>本类只做模型 I/O；落库、护栏、SSE 封帧等编排逻辑在控制器/应用层，
 * 便于未来以新 impl 接入其他模型（策略切换见接口注释）。</p>
 */
@Service
@Slf4j
public class AiCodeHelperHigherImpl implements AiCodeHelperHigherService {

    /** 仅发图片无文字时的默认引导语 */
    private static final String DEFAULT_IMAGE_PROMPT =
            "请仔细观察我上传的图片，结合纺织专业知识进行识别、分析并给出解释。";

    /** 视觉模型名，与面料识别共用同一配置项 */
    @Value("${langChain4j.community.dashscope.vision-model.model-name}")
    private String visionModelName;

    /** 视觉模型 apiKey，缺省复用文本模型的 key（嵌套占位符取默认值） */
    @Value("${langChain4j.community.dashscope.vision-model.api-key}")
    private String apiKey;

    @Override
    public Flux<String> multiModelChat(List<String> imageUrls, String prompt) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Flux.error(new IllegalArgumentException("multiModelChat 至少需要一张图片"));
        }

        // content 顺序即模型阅读顺序：先 N 张图，最后一段文字提问
        List<Map<String, Object>> contents = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("image", imageUrl);
            contents.add(imageContent);
        }
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("text", prompt == null || prompt.isBlank() ? DEFAULT_IMAGE_PROMPT : prompt);
        contents.add(textContent);

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();

        try {
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model(visionModelName)
                    .messages(List.of(userMessage))
                    .incrementalOutput(true) // 增量输出：每帧只含新增文本，前端可直接拼接
                    .build();

            // streamCall 返回 RxJava Flowable（实现 Reactive Streams Publisher），直接桥接 Flux
            return Flux.from(new MultiModalConversation().streamCall(param))
                    .map(this::deltaOf)
                    .filter(text -> !text.isEmpty())
                    .doOnError(e -> log.error("多模态流式调用失败，model={}", visionModelName, e));
        } catch (Exception e) {
            // 构造参数/发起调用阶段的同步异常（NoApiKeyException 等）转为流错误
            log.error("多模态调用发起失败，model={}", visionModelName, e);
            return Flux.error(e);
        }
    }

    /** 取流式帧中的增量文本；无文本的帧（如首帧/末帧）返回空串被 filter 掉 */
    private String deltaOf(MultiModalConversationResult result) {
        if (result == null || result.getOutput() == null) {
            return "";
        }
        List<MultiModalConversationOutput.Choice> choices = result.getOutput().getChoices();
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        MultiModalMessage message = choices.get(0).getMessage();
        if (message == null || message.getContent() == null || message.getContent().isEmpty()) {
            return "";
        }
        Object text = message.getContent().get(0).get("text");
        return text == null ? "" : text.toString();
    }
}
