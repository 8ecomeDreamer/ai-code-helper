package com.example.aicodehelper.ai.app.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 自定义封装的高阶多模态 API（langchain4j 缺少多模态流式的声明式高阶 API，
 * 故直接封装官方 DashScope SDK）。
 *
 * <p>本接口是「模型适配器」契约：只负责模型 I/O（拼装多模态消息、流式调用、
 * 切增量文本），不掺编排逻辑（落库/护栏/SSE 封帧留在控制器/应用层）。
 * 未来接入其他模型时新增一个 impl（用 @ConditionalOnProperty 按
 * textile.vision.provider 切换或 List 注入按 supports() 选择），控制器零改动。</p>
 */
@Tag(name = "AiCodeHelperCustomFactory", description = "自定义封装高阶api")
public interface AiCodeHelperHigherService {

    /**
     * 多模态流式问答：图片（URL 或 base64 data URI）+ 文字 → 逐增量文本输出。
     *
     * @param imageUrls 图片列表（支持 http URL 与 data:image/xxx;base64,xxx）
     * @param prompt    用户文字提问（可为空，空时用默认引导语）
     */
    Flux<String> multiModelChat(List<String> imageUrls, String prompt);

    /** 单图便捷重载 */
    default Flux<String> multiModelChat(String imageUrl, String prompt) {
        return multiModelChat(List.of(imageUrl), prompt);
    }
}
