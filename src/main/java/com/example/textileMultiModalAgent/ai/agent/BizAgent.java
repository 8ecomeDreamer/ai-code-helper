package com.example.textileMultiModalAgent.ai.agent;

import com.example.textileMultiModalAgent.common.core.AiJson;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务 Agent 抽象基类：沉淀五个业务 Agent（问答/报价/调研/合规/面料）的通用能力。
 *
 * <p>分层契约：</p>
 * <ul>
 *   <li>Agent 层持有 prompt 常量、模型 I/O 与输出 JSON 解析，是无状态 {@code @Component} 单例（线程安全），
 *       不碰 DB / Servlet / MultipartFile；</li>
 *   <li>Service 层负责应用编排：输入解析、落库、normalize、降级兜底、summary 重算与响应信封；</li>
 *   <li>与 {@link BaseAgent} 的分工：BaseAgent 是有状态 step 状态机（非线程安全），留给未来多步推理场景；
 *       当前五业务均为单轮/双轮调用，继承本基类即可。</li>
 * </ul>
 */
@Slf4j
public abstract class BizAgent {

    /** 文本模型（qwen-max），子类 prompt 调用统一经此出口 */
    @Resource
    protected ChatModel myQwenChatModel;

    /** 同步文本调用；模型异常原样抛给 Service 层做降级决策 */
    protected String chat(String prompt) {
        return myQwenChatModel.chat(prompt);
    }

    /** 同步文本调用并容错解析 JSON（剥代码围栏 + 括号配对）；解析失败返回 null */
    protected JsonNode chatJson(String prompt) {
        return AiJson.parseTree(chat(prompt));
    }
}
