package com.example.textileMultiModalAgent.service.ai;

import com.example.textileMultiModalAgent.ai.agent.FabricAgent;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 面料疵点检测服务（应用编排层）：两段式视觉审查的 prompt 与 DashScope 多模态调用在
 * {@link FabricAgent}，本服务只负责 dataUri 拼装、报告规整与错误信封。
 *
 * <p>两段式视觉审查：</p>
 * <ol>
 *   <li>门禁判定：图片是否为面料实物特写，非面料直接返回 status=error「检测结果错误」，不浪费第二次调用；</li>
 *   <li>疵点检测：按四分制逐类检查断纬/断经、油污/渍痕、色差/色花、破洞/稀密路、竹节纱/棉结，输出 defects + verdict。</li>
 * </ol>
 *
 * <p>模型侧报错原文透出到 message，不再吞成通用文案。</p>
 *
 * <p>输出形状与前端 FabricView 契约一致：
 * {status: done|error, title, message, report?: {defects[{name,position,grade}], verdict}}。</p>
 */
@Slf4j
@Service
public class FabricService {

    @Resource
    private FabricAgent fabricAgent;

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
            gate = fabricAgent.gate(dataUri);
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
            reportNode = fabricAgent.detect(dataUri);
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
