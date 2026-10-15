package com.example.aicodehelper.service.ai;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.example.aicodehelper.common.core.AiJson;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 面料疵点检测服务：复用综合问答图片链路的 DashScope 多模态通道（MultiModalConversation 同步调用）。
 *
 * <p>两段式视觉审查：</p>
 * <ol>
 *   <li>门禁判定：图片是否为面料实物特写，非面料直接返回 status=error「检测结果错误」，不浪费第二次调用；</li>
 *   <li>疵点检测：按四分制逐类检查断纬/断经、油污/渍痕、色差/色花、破洞/稀密路、竹节纱/棉结，输出 defects + verdict。</li>
 * </ol>
 *
 * <p>模型名读 multi-model 配置（本 Key 渠道 qwen-vl-max 未开通会 404，qwen3-omni-flash 已验证可用）；
 * 模型侧报错原文透出到 message，不再吞成通用文案。</p>
 *
 * <p>输出形状与前端 FabricView 契约一致：
 * {status: done|error, title, message, report?: {defects[{name,position,grade}], verdict}}。</p>
 */
@Slf4j
@Service
public class FabricService {

    /** 视觉模型名：与综合问答图片链路共用 multi-model 配置 */
    @Value("${langChain4j.community.dashscope.multi-model.model-name}")
    private String visionModelName;

    @Value("${langChain4j.community.dashscope.multi-model.api-key}")
    private String apiKey;

    private static final String GATE_PROMPT = """
            你是纺织品检验专家。请判断图片是否为面料/纺织品（梭织、针织、牛仔、印花布等）的实物特写照片。
            只输出一个 JSON 对象，不要解释、不要代码围栏：
            {"isFabric": true或false, "reason": "一句判定依据"}
            """;

    private static final String DETECT_PROMPT = """
            你是纺织品质检专家，按四分制检验图片中的布面质量。逐类检查常见疵点：断纬/断经、油污/渍痕、色差/色花、破洞/稀密路、竹节纱/棉结。
            只输出一个 JSON 对象，不要解释、不要代码围栏：
            {"defects":[{"name":"疵点名称","position":"图中位置描述（如 左中部 (x:120, y:340)）","grade":"严重程度 A/B/C（A 轻微、B 中等、C 严重）"}],"verdict":"一句综合评定，如 B 级品 · 建议降级处理或修复后复检"}
            未发现任何疵点时 defects 为空数组，verdict 给出 A 级评定。
            """;

    /**
     * 面料疵点检测。
     *
     * @param imageBytes  图片字节
     * @param contentType 图片 MIME（如 image/jpeg）
     * @return {status, title, message, report?}
     */
    public Map<String, Object> detect(byte[] imageBytes, String contentType) {
        String dataUri = "data:" + (contentType == null || contentType.isBlank() ? "image/jpeg" : contentType)
                + ";base64," + Base64.getEncoder().encodeToString(imageBytes);

        // 第一段：门禁判定，非面料图片不进入疵点检测
        JsonNode gate;
        try {
            gate = AiJson.parseTree(callVision(dataUri, GATE_PROMPT));
        } catch (Exception e) {
            log.warn("面料门禁判定调用失败：{}", e.getMessage());
            return error("检测服务不可用", "视觉模型调用失败：" + rawMessage(e));
        }
        if (gate == null || !gate.path("isFabric").asBoolean(false)) {
            String reason = gate == null ? "" : gate.path("reason").asText("");
            return error("检测结果错误",
                    "上传的图片不是面料实物照片" + (reason.isBlank() ? "" : "（" + reason + "）") + "，请重新上传平整布面图片。");
        }

        // 第二段：疵点检测
        JsonNode reportNode;
        try {
            reportNode = AiJson.parseTree(callVision(dataUri, DETECT_PROMPT));
        } catch (Exception e) {
            log.warn("疵点检测调用失败：{}", e.getMessage());
            return error("检测失败", "视觉模型调用失败：" + rawMessage(e));
        }
        Map<String, Object> report = normalizeReport(reportNode);
        if (report == null) {
            return error("检测结果错误", "视觉模型返回的报告无法解析，请重试或换一张更清晰的布面图片。");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> defects = (List<Map<String, Object>>) report.get("defects");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "done");
        result.put("title", defects.isEmpty() ? "未检出明显疵点" : "检出 " + defects.size() + " 处疵点");
        result.put("message", defects.isEmpty()
                ? "布面未检出断纬、油污、色差、破洞、竹节纱等明显疵点。"
                : "视觉模型已完成布面审查，疵点定位与等级如下，综合评定见报告尾部。");
        result.put("report", report);
        return result;
    }

    /** 同步多模态调用：一张图 + 一段指令，返回模型全文文本 */
    private String callVision(String dataUri, String prompt) throws Exception {
        List<Map<String, Object>> contents = List.of(Map.of("image", dataUri), Map.of("text", prompt));
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(contents)
                .build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(visionModelName)
                .messages(List.of(userMessage))
                .incrementalOutput(false) // 非流式：单帧返回全文，便于 JSON 解析
                .build();
        MultiModalConversationResult result = new MultiModalConversation().call(param);
        if (result == null || result.getOutput() == null) {
            throw new IllegalStateException("视觉模型返回空结果");
        }
        List<MultiModalConversationOutput.Choice> choices = result.getOutput().getChoices();
        if (choices == null || choices.isEmpty() || choices.get(0).getMessage() == null) {
            throw new IllegalStateException("视觉模型返回空 choices");
        }
        StringBuilder text = new StringBuilder();
        for (Map<String, Object> content : choices.get(0).getMessage().getContent()) {
            Object value = content.get("text");
            if (value != null) {
                text.append(value);
            }
        }
        return text.toString();
    }

    /** 规整模型报告：defects 字段兜底、grade 归一 A/B/C、verdict 缺失时按最重等级生成 */
    private Map<String, Object> normalizeReport(JsonNode node) {
        if (node == null || !node.isObject() || !node.path("defects").isArray()) {
            return null;
        }
        List<Map<String, Object>> defects = new ArrayList<>();
        String worst = "A";
        for (JsonNode entry : node.path("defects")) {
            String name = entry.path("name").asText("");
            if (name.isBlank()) {
                continue;
            }
            String grade = normalizeGrade(entry.path("grade").asText("B"));
            if (grade.compareTo(worst) > 0) {
                worst = grade;
            }
            Map<String, Object> defect = new LinkedHashMap<>();
            defect.put("name", name);
            defect.put("position", entry.path("position").asText("未定位"));
            defect.put("grade", grade);
            defects.add(defect);
        }
        String verdict = node.path("verdict").asText("");
        if (verdict.isBlank()) {
            verdict = defects.isEmpty() ? "A 级品 · 布面质量良好" : worst + " 级品 · 建议按四分制评分复核处理";
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("defects", defects);
        report.put("verdict", verdict);
        return report;
    }

    private String normalizeGrade(String grade) {
        String upper = grade.trim().toUpperCase();
        return upper.startsWith("A") ? "A" : upper.startsWith("B") ? "B" : upper.startsWith("C") ? "C" : "B";
    }

    private Map<String, Object> error(String title, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "error");
        result.put("title", title);
        result.put("message", message);
        return result;
    }

    /** 透出模型侧原始报错（如 404 Model not exist），避免吞成通用文案掩盖根因 */
    private String rawMessage(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
