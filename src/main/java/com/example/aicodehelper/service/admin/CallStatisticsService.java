package com.example.aicodehelper.service.admin;

import com.example.aicodehelper.common.core.AiJson;
import com.example.aicodehelper.domain.AiBusinessLog;
import com.example.aicodehelper.service.ai.AiBusinessLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 调用统计服务：把「模块 API 调用」与「百炼模型调用」两类用量异步落到 ai_business_log，
 * 供后台 Dashboard 聚合展示（log_type = api_call / model_call，复用现有表，无新增迁移）。
 *
 * <p>所有写入均为异步且吞异常：统计是旁路能力，绝不阻塞、绝不拖垮主业务链路。</p>
 */
@Service
@Slf4j
public class CallStatisticsService {

    /** 日志类型：模块 API 调用（agent_name=模块名，log_content={"status":..,"detail":..}） */
    public static final String TYPE_API_CALL = "api_call";

    /** 日志类型：百炼模型调用（agent_name=业务场景，log_content={"model":..,"in":..,"out":..,"ok":..}） */
    public static final String TYPE_MODEL_CALL = "model_call";

    @Resource
    private AiBusinessLogService aiBusinessLogService;

    /**
     * 记录一次模块 API 调用。
     *
     * @param module     模块名（写 agent_name，如「综合问答」）
     * @param httpStatus HTTP 状态码
     * @param costMs     耗时毫秒
     * @param detail     附加说明（方法 + 路径）
     */
    public void recordApiCall(String module, int httpStatus, long costMs, String detail) {
        AiBusinessLog row = baseRow(TYPE_API_CALL, module, costMs);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("status", httpStatus);
        content.put("detail", detail);
        row.setLog_content(toJson(content));
        saveAsync(row);
    }

    /**
     * 记录一次百炼模型调用。
     *
     * @param scene   业务场景（写 agent_name，如「面料疵点检测」）
     * @param model   模型名
     * @param costMs  耗时毫秒
     * @param in      输入 token，未知传 null
     * @param out     输出 token，未知传 null
     * @param success 是否成功
     */
    public void recordModelCall(String scene, String model, long costMs, Integer in, Integer out, boolean success) {
        AiBusinessLog row = baseRow(TYPE_MODEL_CALL, scene, costMs);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("model", model);
        content.put("in", in);
        content.put("out", out);
        content.put("ok", success);
        row.setLog_content(toJson(content));
        saveAsync(row);
    }

    private AiBusinessLog baseRow(String type, String agent, long costMs) {
        AiBusinessLog row = new AiBusinessLog();
        row.setLog_type(type);
        row.setAgent_name(agent);
        row.setCost_ms(costMs);
        row.setCreate_time(new Date());
        return row;
    }

    private String toJson(Map<String, Object> content) {
        try {
            return AiJson.mapper().writeValueAsString(content);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void saveAsync(AiBusinessLog row) {
        CompletableFuture.runAsync(() -> {
            try {
                aiBusinessLogService.save(row);
            } catch (Exception e) {
                log.warn("写入调用统计失败，type={}, agent={}", row.getLog_type(), row.getAgent_name(), e);
            }
        });
    }
}
