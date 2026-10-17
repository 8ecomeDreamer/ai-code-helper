package com.example.aicodehelper.service.ai;

import com.example.aicodehelper.ai.agent.CompetitorAgent;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 竞品调研服务（应用编排层）：按面料关键词聚合多平台竞品行情。
 *
 * <p>prompt 与模型 I/O 在 {@link CompetitorAgent}，本服务负责三级降级链路：</p>
 * <ol>
 *   <li>联网搜索 MCP（百度 ai_search）+ AiServices 工具循环：模型多轮检索各平台后聚合，实时性最好；</li>
 *   <li>MCP 未配置或调用失败：模型自有行业知识离线给出行情区间（analysis 中注明数据可能滞后）；</li>
 *   <li>模型也失败：内置演示数据（demo=true），保证页面永远有结果。</li>
 * </ol>
 *
 * <p>输出形状与前端 ResearchView 契约一致：
 * {fabric, competitors[{platform,supplier,region,currency,price,unit,moq,leadTime,rating}], analysis[], source, demo}，
 * 其中 price/rating 强制为数值（前端直接 toFixed）。</p>
 */
@Slf4j
@Service
public class ResearchService {

    @Resource
    private CompetitorAgent competitorAgent;

    /**
     * 按面料关键词调研竞品行情。
     *
     * @param keyword 面料关键词，如：全棉府绸
     * @return 比价报告 Map；任何情况下都不返回 null
     */
    public Map<String, Object> research(String keyword) {
        if (competitorAgent.mcpAvailable()) {
            try {
                Map<String, Object> report = normalize(competitorAgent.researchOnline(keyword), keyword, "mcp");
                if (report != null) {
                    return report;
                }
                log.warn("MCP 联网调研未返回有效竞品，降级模型离线知识，keyword={}", keyword);
            } catch (Exception e) {
                log.warn("MCP 联网调研失败，降级模型离线知识：{}", e.getMessage());
            }
        } else {
            log.info("联网搜索 MCP 未配置，竞品调研使用模型离线知识，keyword={}", keyword);
        }
        try {
            Map<String, Object> report = normalize(competitorAgent.researchOffline(keyword), keyword, "model");
            if (report != null) {
                return report;
            }
        } catch (Exception e) {
            log.warn("模型离线调研失败，降级内置演示数据：{}", e.getMessage());
        }
        return demo(keyword);
    }

    /* ---------------- 结果归一化（price/rating 强制数值） ---------------- */

    private Map<String, Object> normalize(JsonNode node, String keyword, String source) {
        if (node == null || !node.isObject()) {
            return null;
        }
        List<Map<String, Object>> competitors = new ArrayList<>();
        for (JsonNode item : node.path("competitors")) {
            double price = item.path("price").asDouble(0);
            String platform = item.path("platform").asText("");
            if (platform.isBlank() || price <= 0) {
                continue;
            }
            String currency = "CNY".equals(item.path("currency").asText("USD")) ? "CNY" : "USD";
            double rating = item.path("rating").asDouble(0);
            Map<String, Object> competitor = new LinkedHashMap<>();
            competitor.put("platform", platform);
            competitor.put("supplier", item.path("supplier").asText(""));
            competitor.put("region", item.path("region").asText(""));
            competitor.put("currency", currency);
            competitor.put("price", price);
            competitor.put("unit", item.path("unit").asText("/米"));
            competitor.put("moq", item.path("moq").asText(""));
            competitor.put("leadTime", item.path("leadTime").asText(""));
            competitor.put("rating", rating > 0 ? Math.min(rating, 5D) : 4.5D);
            competitors.add(competitor);
        }
        if (competitors.isEmpty()) {
            return null;
        }
        List<String> analysis = new ArrayList<>();
        JsonNode analysisNode = node.path("analysis");
        if (analysisNode.isArray()) {
            analysisNode.forEach(line -> {
                if (!line.asText("").isBlank()) {
                    analysis.add(line.asText());
                }
            });
        } else if (!analysisNode.asText("").isBlank()) {
            analysis.add(analysisNode.asText());
        }
        if (analysis.isEmpty()) {
            analysis.add("已聚合 " + competitors.size() + " 个平台竞品，建议结合 ERP 成本与目标利润率最终定价。");
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("fabric", node.path("fabric").asText(keyword));
        report.put("competitors", competitors);
        report.put("analysis", analysis);
        report.put("source", source);
        report.put("demo", false);
        return report;
    }

    /* ---------------- 内置演示数据（最后兜底） ---------------- */

    private Map<String, Object> demo(String keyword) {
        List<Map<String, Object>> competitors = new ArrayList<>();
        competitors.add(competitor("阿里国际站", "绍兴柯桥轻纺城供应商", "浙江绍兴", "USD", 1.85, "1000 米", "25 天", 4.6));
        competitors.add(competitor("Made-in-China", "南通棉纺集团", "江苏南通", "USD", 1.72, "1500 米", "30 天", 4.4));
        competitors.add(competitor("全球纺织网", "盛泽面料市场商户", "江苏吴江", "CNY", 12.60, "800 米", "20 天", 4.2));
        competitors.add(competitor("阿里国际站", "广州中大布匹市场", "广东广州", "USD", 1.90, "500 米", "15 天", 4.7));
        competitors.add(competitor("Made-in-China", "潍坊染织有限公司", "山东潍坊", "CNY", 11.80, "2000 米", "35 天", 4.3));
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("fabric", keyword);
        report.put("competitors", competitors);
        report.put("analysis", List.of(
                "「" + keyword + "」主流成交区间：USD 1.70-1.95/米（FOB），CNY 11.5-13.0/米（内销）。",
                "阿里国际站供应商交期普遍 15-25 天，起订量低于 Made-in-China 渠道，适合中小单。",
                "建议我司报价落在区间中位偏上（品质背书），并以交期优势争取急单客户。",
                "当前数据为内置演示数据（联网调研与模型知识均不可用），仅供页面联调参考。"));
        report.put("source", "demo");
        report.put("demo", true);
        return report;
    }

    private Map<String, Object> competitor(String platform, String supplier, String region,
                                           String currency, double price, String moq, String leadTime, double rating) {
        Map<String, Object> competitor = new LinkedHashMap<>();
        competitor.put("platform", platform);
        competitor.put("supplier", supplier);
        competitor.put("region", region);
        competitor.put("currency", currency);
        competitor.put("price", price);
        competitor.put("unit", "/米");
        competitor.put("moq", moq);
        competitor.put("leadTime", leadTime);
        competitor.put("rating", rating);
        return competitor;
    }
}
