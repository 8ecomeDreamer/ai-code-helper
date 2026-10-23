package com.example.textileMultiModalAgent.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 合规校验 Agent：持有 generator/reflection 双轮 prompt 与模型 I/O。
 *
 * <p>Reflection 范式：generator 出初稿 → reflection 一轮对照原文纠偏
 * （passed 误判 / 漏检风险 / 意见脱离原文）。规则引擎基线、合并与 summary 重算在 ComplianceService。</p>
 */
@Slf4j
@Component
public class ComplianceAgent extends BizAgent {

    /** 送入模型的条款文本上限（控制 token 成本） */
    private static final int MAX_PROMPT_CHARS = 12000;

    /**
     * 初稿审查：逐条审查 10 类必备条款与高风险表述。
     *
     * @return 解析后的 {items,risks} JSON；模型失败/解析失败返回 null
     */
    public JsonNode draft(String content) {
        try {
            return chatJson(generatorPrompt(content));
        } catch (Exception e) {
            log.warn("合规审查初稿调用失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * Reflection 纠偏轮：对照原文复核初稿并输出修订版。
     *
     * @return 修订后的 {items,risks} JSON；失败返回 null（Service 沿用初稿）
     */
    public JsonNode reflect(String content, String draftJson) {
        try {
            return chatJson(reflectionPrompt(content, draftJson));
        } catch (Exception e) {
            log.warn("合规审查 reflection 轮失败：{}", e.getMessage());
            return null;
        }
    }

    /* ---------------- Prompt ---------------- */

    private String generatorPrompt(String content) {
        return """
                你是纺织外贸行业的合同合规审查专家，请逐条审查下面的合同条款，输出合规审查报告。
                审查维度固定为 10 个必备条款项（id 不可更改）：composition 成分标注 / incoterms 贸易术语 / inspection 检验标准 / payment 支付 / delivery 交期与延迟责任 / force-majeure 不可抗力 / dispute 争议解决 / ip-confidential 知识产权与保密 / packing 包装与唛头 / quantity-tolerance 数量溢短装。
                每项给出 passed（条款是否实质覆盖该维度，仅看原文依据，不得臆断）与 detail（一句审查意见；未覆盖时说明风险与修订建议）。
                risks 为高风险表述（如无限责任、对方单方面变更授权、违约金≥10%、发货前 100% 全款预付等），每项 {id,name,advice,hits}，hits 为原文摘录（最多 3 条）；没有则空数组。
                只输出一个 JSON 对象，不要解释、不要代码围栏：
                {"items":[{"id","name","level","passed":布尔,"detail"}],"risks":[{"id","name","advice","hits":[]}]}
                合同条款：
                <<<
                %s
                >>>
                """.formatted(abbreviate(content));
    }

    private String reflectionPrompt(String content, String draft) {
        return """
                下面是合同条款原文与一份合规审查初稿。请对照原文复核初稿：passed 判定是否有原文依据、是否漏检高风险表述、detail 是否脱离原文，并输出修订后的完整 JSON（结构不变：{items,risks}，只输出 JSON）。
                [初稿]
                %s
                [原文]
                <<<
                %s
                >>>
                """.formatted(draft, abbreviate(content));
    }

    private String abbreviate(String content) {
        return content.length() > MAX_PROMPT_CHARS ? content.substring(0, MAX_PROMPT_CHARS) + "\n...(条款过长已截断)" : content;
    }
}
