package com.example.textileMultiModalAgent.ai.app;

/**
 * 竞品调研 Assistant。
 *
 * <p>挂载百度 ai_search 联网搜索 MCP 工具（McpConfig.mcpToolProvider），
 * 由模型自主决定多轮检索（阿里国际站 / Made-in-China / 全球纺织网等）后聚合比价报告。</p>
 */
public interface ResearchAssistant {

    /**
     * 执行调研并返回报告 JSON 文本。
     *
     * @param prompt 调研指令（含关键词与输出 JSON 形状约束）
     * @return 模型聚合后的 JSON 文本
     */
    String research(String prompt);
}
