package com.example.textileMultiModalAgent.ai.agent;

import com.example.textileMultiModalAgent.service.ai.ResearchAssistant;
import com.example.textileMultiModalAgent.common.core.AiJson;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 竞品调研 Agent：持有调研 prompt、联网搜索 MCP 装配与模型 I/O。
 *
 * <p>两档调研能力：</p>
 * <ul>
 *   <li>联网档：百度 ai_search MCP + AiServices 工具循环，模型多轮检索各平台后聚合；</li>
 *   <li>离线档：模型自有行业知识给出行情区间。</li>
 * </ul>
 *
 * <p>只返回解析后的 JSON 节点（失败 null/抛异常），normalize 与三级降级在 ResearchService。</p>
 */
@Slf4j
@Component
public class CompetitorAgent extends BizAgent {

    /** MCP 未配置时 Bean 为 null，用 ObjectProvider 安全获取 */
    @Resource
    private ObjectProvider<McpToolProvider> mcpToolProviderProvider;

    /** MCP 版 Assistant 懒加载单例（工具循环较重，避免每次请求重建） */
    private volatile ResearchAssistant mcpAssistant;

    /** 联网搜索 MCP 是否可用 */
    public boolean mcpAvailable() {
        return mcpToolProviderProvider.getIfAvailable() != null;
    }

    /**
     * 联网调研：MCP 工具循环后聚合输出；解析失败返回 null。
     *
     * @throws Exception MCP/模型调用失败，由 Service 降级
     */
    public JsonNode researchOnline(String keyword) throws Exception {
        McpToolProvider provider = mcpToolProviderProvider.getIfAvailable();
        if (provider == null) {
            return null;
        }
        return AiJson.parseTree(assistant(provider).research(prompt(keyword, true)));
    }

    /** 离线调研：模型自有知识；解析失败返回 null */
    public JsonNode researchOffline(String keyword) {
        return chatJson(prompt(keyword, false));
    }

    /* ---------------- MCP Assistant 装配 ---------------- */

    private ResearchAssistant assistant(McpToolProvider provider) {
        ResearchAssistant assistant = mcpAssistant;
        if (assistant == null) {
            synchronized (this) {
                assistant = mcpAssistant;
                if (assistant == null) {
                    assistant = AiServices.builder(ResearchAssistant.class)
                            .chatModel(myQwenChatModel)
                            .toolProvider(provider)
                            .build();
                    mcpAssistant = assistant;
                }
            }
        }
        return assistant;
    }

    /* ---------------- Prompt ---------------- */

    private String prompt(String keyword, boolean online) {
        String searchHint = online
                ? "先调用联网搜索工具，分别检索阿里国际站、Made-in-China、全球纺织网等平台上该面料的最新竞品报价（至少检索 2 次不同平台），再聚合输出。"
                : "不要调用任何工具；基于你的纺织行业知识给出各平台代表性行情区间，并在 analysis 中注明数据来自模型知识、可能滞后。";
        return """
                你是纺织外贸行情分析师，请调研面料「%s」在各外部平台的竞品行情并输出对比报告。
                %s
                只输出一个 JSON 对象，不要解释、不要代码围栏：
                {"fabric":"面料标准名称","competitors":[{"platform":"平台名","supplier":"供应商或产区","region":"地区","currency":"USD或CNY","price":数字单价,"unit":"/米","moq":"起订量","leadTime":"交期","rating":数字评分0到5}],"analysis":["调研结论与报价建议1","2","3"]}
                要求：competitors 至少 4 条且覆盖至少 3 个不同平台；price 与 rating 必须是纯数字（不带货币符号与文字）；analysis 3-5 条，最后一条给出对我司该面料的报价建议区间。
                """.formatted(keyword, searchHint);
    }
}
