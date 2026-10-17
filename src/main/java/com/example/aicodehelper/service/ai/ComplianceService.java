package com.example.aicodehelper.service.ai;

import com.example.aicodehelper.ai.agent.ComplianceAgent;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 合同条款合规校验服务。
 *
 * <p>两级审查 + 自洽汇总：</p>
 * <ol>
 *   <li>Java 规则引擎基线：10 类必备条款关键词匹配 + 4 类高风险表述正则（与前端 compliance.js 规则同构，确定性兜底）；</li>
 *   <li>大模型深度审查：generator 出初稿 → reflection 一轮对照原文纠偏（prompt 与模型 I/O 在 {@link ComplianceAgent}）；</li>
 *   <li>summary 一律在 Java 端按最终 items/risks 重算，保证计数与结论自洽；模型不可用时整体降级规则引擎。</li>
 * </ol>
 *
 * <p>输出形状与前端 ComplianceView 契约一致：
 * {items[{id,name,level,passed,detail}], risks[{id,name,advice,hits[]}], summary{total,passed,missingError,missingWarn,missingInfo,riskCount,verdict,verdictText}, source}。</p>
 */
@Slf4j
@Service
public class ComplianceService {

    @Resource
    private ComplianceAgent complianceAgent;

    /** 必备条款规则（id/level/关键词/缺失与命中意见，与前端规则集一致） */
    private record Rule(String id, String name, String level, List<String> keywords, String missing, String present) {
    }

    /** 高风险表述规则（命中即提示） */
    private record RiskRule(String id, String name, Pattern pattern, String advice) {
    }

    /** 模型审查单项结果 */
    private record LlmItem(boolean passed, String detail) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule("composition", "成分标注条款", "error",
                    List.of("成分", "纤维含量", "composition", "面料材质", "材质"),
                    "条款中未发现纤维成分/含量约定。纺织品成分标注是海关查验与买家索赔的高频争议点，必须明确成分比例及允许偏差（如 ±3%）。",
                    "已包含成分标注约定。建议核对是否写明成分允许偏差与检测方法（如 FZ/T 01057 / ISO 1833）。"),
            new Rule("incoterms", "贸易术语（Incoterms）", "error",
                    List.of("fob", "cif", "exw", "ddp", "cfr", "贸易术语", "incoterms"),
                    "未检测到贸易术语（FOB/CIF/EXW 等）。缺少术语将导致运费、保险与风险转移界限不清，极易引发纠纷。",
                    "已明确贸易术语。建议同时注明术语版本（Incoterms 2020）与具体港口/地点。"),
            new Rule("inspection", "检验标准条款", "error",
                    List.of("检验", "检测", "验布", "inspection", "gb", "aatcc", "iso", "oeko", "sgs", "质量标准"),
                    "未约定检验标准与验货方式。建议明确执行标准（GB/FZT/AATCC/ISO）、抽检比例（如四分制 10% 抽检）与第三方验货机构。",
                    "已包含检验标准约定。建议确认抽检比例、允收水准（AQL）与复验流程是否完整。"),
            new Rule("payment", "支付条款", "error",
                    List.of("付款", "支付", "payment", "t/t", "l/c", "信用证", "定金", "预付款", "尾款", "电汇"),
                    "未检测到支付条款。缺少付款方式（T/T、L/C 等）与付款节点将直接影响收汇安全。",
                    "已包含支付条款。建议核对预付款比例（常见 30%）、尾款节点（发货前/见提单副本）及逾期付款利息约定。"),
            new Rule("delivery", "交期与延迟责任", "warn",
                    List.of("交货", "交期", "delivery", "装运", "shipment", "延迟", "延误"),
                    "未明确交货期或延迟交付责任。建议约定交货天数基准（如收到预付款后 X 天）与延迟违约金上限。",
                    "已包含交期约定。建议确认延迟违约金比例（常见每周 0.5%，上限 5%）与宽限期条款。"),
            new Rule("force-majeure", "不可抗力条款", "warn",
                    List.of("不可抗力", "force majeure", "天灾", "战争", "疫情"),
                    "缺少不可抗力条款。发生突发事件时双方责任界限不清，建议补充定义、通知义务与解除条件。",
                    "已包含不可抗力条款。建议确认通知时限（如 7 日内书面通知）与证明机构（贸促会 CCPIT）。"),
            new Rule("dispute", "争议解决条款", "warn",
                    List.of("仲裁", "争议", "管辖", "arbitration", "dispute", "诉讼", "cietaac"),
                    "未约定争议解决方式。建议明确仲裁机构（如 CIETAC 中国国际经济贸易仲裁委员会）或管辖法院及适用法律。",
                    "已包含争议解决约定。建议确认适用法律（中国法/第三方法律）与仲裁地是否对己方有利。"),
            new Rule("ip-confidential", "知识产权与保密", "info",
                    List.of("知识产权", "保密", "花样", "花型", "专利", "confidential", "ip ", "版权"),
                    "未发现知识产权/保密条款。涉及定制花型、印花图案时建议约定花样版权归属与保密义务。",
                    "已包含知识产权或保密约定，风险较低。"),
            new Rule("packing", "包装与唛头", "info",
                    List.of("包装", "唛头", "packing", "shipping mark", "卷装", "匹装"),
                    "未约定包装方式与唛头。纺织品海运受潮、串色风险较高，建议明确卷装/匹装、防潮材料与唛头内容。",
                    "已包含包装/唛头约定。建议核对是否注明防潮、防晒等防护要求。"),
            new Rule("quantity-tolerance", "数量溢短装", "info",
                    List.of("溢短装", "更多或更少", "more or less", "tolerance", "数量偏差", "±"),
                    "未约定数量溢短装条款。面料生产存在自然缸差与米数偏差，建议约定 ±3%-5% 溢短装及按实结算方式。",
                    "已包含数量偏差/溢短装约定，风险较低。"));

    private static final List<RiskRule> RISK_RULES = List.of(
            new RiskRule("unlimited-liability", "无限连带责任",
                    Pattern.compile("(承担)?(一切|全部|无限)(责任|损失|赔偿)"),
                    "条款中出现\"承担一切/无限责任\"类表述，责任范围无上限，建议修改为以合同金额为限的赔偿责任。"),
            new RiskRule("oaid-blank", "空白授权",
                    Pattern.compile("(买方|甲方)(有权|可以)(自行|单方面)(决定|变更|修改|取消)"),
                    "存在对方单方面变更/取消合同的授权表述，建议补充\"经双方书面协商一致\"前置条件。"),
            new RiskRule("penalty-high", "高额违约金",
                    Pattern.compile("违约金[^。；\\n]*?(\\d{2,})\\s*%"),
                    "检测到较高违约金比例（≥10%），建议核对是否超出行业惯例（延迟交付常见每周 0.5%、上限 5%）。"),
            new RiskRule("payment-full-advance", "全款预付风险",
                    Pattern.compile("(发货前|交货前)[^。；\\n]*?(100\\s*%|全部|全额)(付款|支付)"),
                    "作为卖方需确认\"发货前付清全款\"的可执行性；作为买方则该条款资金风险较高，建议改为 30% 预付 + 尾款见提单副本。"));

    /**
     * 对条款文本执行合规校验。
     *
     * @param content 条款文本（控制器已保证非空；pdf 由 DocumentParseService 解析）
     * @return 校验报告 Map
     */
    public Map<String, Object> check(String content) {
        List<Map<String, Object>> baselineItems = baselineItems(content);
        List<Map<String, Object>> baselineRisks = baselineRisks(content);
        JsonNode draft = complianceAgent.draft(content);
        Map<String, LlmItem> llmItems = parseLlmItems(draft);
        if (llmItems != null) {
            List<Map<String, Object>> llmRisks = parseLlmRisks(draft);
            // Reflection 一轮：对照原文纠偏初稿，失败则沿用初稿（Agent 内部已兜底 null）
            JsonNode revised = complianceAgent.reflect(content, draft.toString());
            Map<String, LlmItem> revisedItems = parseLlmItems(revised);
            if (revisedItems != null) {
                llmItems = revisedItems;
                llmRisks = parseLlmRisks(revised);
            }
            return report(mergeItems(baselineItems, llmItems), mergeRisks(baselineRisks, llmRisks), "llm");
        }
        log.warn("合规审查模型不可用或输出无效，降级规则引擎");
        return report(baselineItems, baselineRisks, "rules");
    }

    /* ---------------- 规则引擎基线 ---------------- */

    private List<Map<String, Object>> baselineItems(String content) {
        String lower = content.toLowerCase();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Rule rule : RULES) {
            boolean hit = rule.keywords().stream().anyMatch(key -> lower.contains(key.toLowerCase()));
            items.add(item(rule, hit, hit ? rule.present() : rule.missing()));
        }
        return items;
    }

    private List<Map<String, Object>> baselineRisks(String content) {
        List<Map<String, Object>> risks = new ArrayList<>();
        for (RiskRule riskRule : RISK_RULES) {
            Matcher matcher = riskRule.pattern().matcher(content);
            List<String> hits = new ArrayList<>();
            while (matcher.find() && hits.size() < 3) {
                hits.add(matcher.group());
            }
            if (!hits.isEmpty()) {
                Map<String, Object> risk = new LinkedHashMap<>();
                risk.put("id", riskRule.id());
                risk.put("name", riskRule.name());
                risk.put("advice", riskRule.advice());
                risk.put("hits", hits);
                risks.add(risk);
            }
        }
        return risks;
    }

    private Map<String, Object> item(Rule rule, boolean passed, String detail) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", rule.id());
        item.put("name", rule.name());
        item.put("level", rule.level());
        item.put("passed", passed);
        item.put("detail", detail);
        return item;
    }

    /* ---------------- 模型审查结果解析与合并 ---------------- */

    private Map<String, LlmItem> parseLlmItems(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode itemsNode = node.path("items");
        if (!itemsNode.isArray() || itemsNode.isEmpty()) {
            return null;
        }
        Map<String, LlmItem> llmItems = new HashMap<>();
        for (JsonNode entry : itemsNode) {
            String id = entry.path("id").asText("");
            if (id.isBlank()) {
                continue;
            }
            llmItems.put(id, new LlmItem(entry.path("passed").asBoolean(false), entry.path("detail").asText("")));
        }
        return llmItems.isEmpty() ? null : llmItems;
    }

    private List<Map<String, Object>> parseLlmRisks(JsonNode node) {
        List<Map<String, Object>> risks = new ArrayList<>();
        if (node == null || !node.isObject()) {
            return risks;
        }
        for (JsonNode entry : node.path("risks")) {
            String id = entry.path("id").asText("");
            String name = entry.path("name").asText("");
            String advice = entry.path("advice").asText("");
            if (id.isBlank() || name.isBlank() || advice.isBlank()) {
                continue;
            }
            List<String> hits = new ArrayList<>();
            entry.path("hits").forEach(hit -> {
                if (hits.size() < 3 && !hit.asText("").isBlank()) {
                    hits.add(hit.asText());
                }
            });
            Map<String, Object> risk = new LinkedHashMap<>();
            risk.put("id", id);
            risk.put("name", name);
            risk.put("advice", advice);
            risk.put("hits", hits);
            risks.add(risk);
        }
        return risks;
    }

    /** 模型意见覆盖规则基线的 passed/detail；模型缺项回退基线意见 */
    private List<Map<String, Object>> mergeItems(List<Map<String, Object>> baselineItems, Map<String, LlmItem> llmItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map<String, Object> baseline : baselineItems) {
            String id = String.valueOf(baseline.get("id"));
            LlmItem llm = llmItems.get(id);
            if (llm == null) {
                items.add(baseline);
                continue;
            }
            Map<String, Object> merged = new LinkedHashMap<>(baseline);
            merged.put("passed", llm.passed());
            if (!llm.detail().isBlank()) {
                merged.put("detail", llm.detail());
            }
            items.add(merged);
        }
        return items;
    }

    /** 正则命中为事实依据必须保留，模型新增风险追加其后 */
    private List<Map<String, Object>> mergeRisks(List<Map<String, Object>> baselineRisks, List<Map<String, Object>> llmRisks) {
        List<Map<String, Object>> risks = new ArrayList<>(baselineRisks);
        for (Map<String, Object> llmRisk : llmRisks) {
            boolean exists = risks.stream().anyMatch(r -> String.valueOf(r.get("id")).equals(String.valueOf(llmRisk.get("id"))));
            if (!exists) {
                risks.add(llmRisk);
            }
        }
        return risks;
    }

    /* ---------------- 汇总重算（Java 端保证自洽） ---------------- */

    private Map<String, Object> report(List<Map<String, Object>> items, List<Map<String, Object>> risks, String source) {
        int passed = 0;
        int missingError = 0;
        int missingWarn = 0;
        int missingInfo = 0;
        for (Map<String, Object> item : items) {
            boolean ok = Boolean.TRUE.equals(item.get("passed"));
            if (ok) {
                passed++;
                continue;
            }
            switch (String.valueOf(item.get("level"))) {
                case "error" -> missingError++;
                case "warn" -> missingWarn++;
                default -> missingInfo++;
            }
        }
        String verdict;
        String verdictText;
        if (!risks.isEmpty() || missingError > 0) {
            verdict = "reject";
            verdictText = "发现 " + missingError + " 项高风险条款缺失、" + risks.size() + " 处风险表述，不建议直接签署，请逐项修订后复检。";
        } else if (missingWarn > 0) {
            verdict = "review";
            verdictText = "核心条款完备，但存在 " + missingWarn + " 项建议完善内容，请法务确认后再签署。";
        } else {
            verdict = "pass";
            verdictText = "条款整体完备，未发现高风险缺失项，可进入签署流程。";
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", items.size());
        summary.put("passed", passed);
        summary.put("missingError", missingError);
        summary.put("missingWarn", missingWarn);
        summary.put("missingInfo", missingInfo);
        summary.put("riskCount", risks.size());
        summary.put("verdict", verdict);
        summary.put("verdictText", verdictText);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("items", items);
        report.put("risks", risks);
        report.put("summary", summary);
        report.put("source", source);
        return report;
    }
}
