package com.example.textileMultiModalAgent.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.textileMultiModalAgent.common.core.AiJson;
import com.example.textileMultiModalAgent.common.core.Result;
import com.example.textileMultiModalAgent.domain.AiBusinessLog;
import com.example.textileMultiModalAgent.domain.AiChatMessage;
import com.example.textileMultiModalAgent.domain.SysUser;
import com.example.textileMultiModalAgent.mapper.SysUserMapper;
import com.example.textileMultiModalAgent.service.ai.AiBusinessLogService;
import com.example.textileMultiModalAgent.service.ai.AiChatMessageService;
import com.example.textileMultiModalAgent.service.ai.AiChatSessionService;
import com.example.textileMultiModalAgent.service.admin.CallStatisticsService;
import com.fasterxml.jackson.databind.JsonNode;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.messages.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台 Dashboard 统计聚合接口：一次请求返回核心指标 + 模块调用量 + 百炼用量 + 最近记录。
 *
 * <p>数据来源：</p>
 * <ul>
 *   <li>提问数量 / 会话总数：ai_chat_message（role=user）、ai_chat_session；</li>
 *   <li>注册用户：sys_user（del_flag=0）；</li>
 *   <li>知识库文档：MinIO bucket 内 knowledge/ 前缀对象数（无独立表，异常回退 0）；</li>
 *   <li>模块调用量 / 百炼用量：ai_business_log（log_type=api_call / model_call，见 CallStatisticsService）。</li>
 * </ul>
 *
 * <p>分组统计在内存完成（Dashboard 场景日志量可控），避免自定义 SQL 与多库方言问题。</p>
 */
@RestController
@RequestMapping("/agent")
@Tag(name = "AdminStatsController", description = "后台 Dashboard 统计")
@Slf4j
public class AdminStatsController {

    /** 最近记录展示条数上限 */
    private static final int RECENT_LIMIT = 20;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private AiChatMessageService aiChatMessageService;

    @Resource
    private AiChatSessionService aiChatSessionService;

    @Resource
    private AiBusinessLogService aiBusinessLogService;

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /** 知识库文档在 MinIO 中的对象前缀 */
    @Value("${knowledge.prefix:knowledge/}")
    private String knowledgePrefix;

    @Operation(summary = "Dashboard 聚合统计", description = "核心指标 + 模块调用量 + 百炼用量 + 最近记录")
    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("questionCount", aiChatMessageService.lambdaQuery()
                .eq(AiChatMessage::getRole, "user").count());
        overview.put("sessionCount", aiChatSessionService.count());
        overview.put("userCount", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDel_flag, "0")));
        overview.put("knowledgeDocCount", countKnowledgeDocs());

        List<AiBusinessLog> apiLogs = listByType(CallStatisticsService.TYPE_API_CALL);
        List<AiBusinessLog> modelLogs = listByType(CallStatisticsService.TYPE_MODEL_CALL);

        return Result.success()
                .put("overview", overview)
                .put("apiCalls", aggregateApiCalls(apiLogs))
                .put("modelCalls", aggregateModelCalls(modelLogs))
                .put("recent", recentLogs(apiLogs, modelLogs));
    }

    /** MinIO knowledge/ 前缀对象计数；服务不可达等异常回退 0，不阻塞 Dashboard */
    private long countKnowledgeDocs() {
        try {
            long count = 0;
            Iterable<io.minio.Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(knowledgePrefix).recursive(true).build());
            for (io.minio.Result<Item> item : objects) {
                if (item.get().objectName() != null && !item.get().objectName().endsWith("/")) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            log.warn("统计知识库文档数失败，bucket={}, prefix={}", bucketName, knowledgePrefix, e);
            return 0;
        }
    }

    private List<AiBusinessLog> listByType(String logType) {
        return aiBusinessLogService.lambdaQuery()
                .eq(AiBusinessLog::getLog_type, logType)
                .orderByDesc(AiBusinessLog::getId)
                .list();
    }

    /** 模块调用量：按 agent_name 分组，输出 {name, count, avgCost}，调用量倒序 */
    private List<Map<String, Object>> aggregateApiCalls(List<AiBusinessLog> logs) {
        Map<String, long[]> grouped = new LinkedHashMap<>(); // agent -> [count, costSum]
        for (AiBusinessLog row : logs) {
            String agent = row.getAgent_name() == null ? "未知模块" : row.getAgent_name();
            long[] acc = grouped.computeIfAbsent(agent, key -> new long[2]);
            acc[0]++;
            acc[1] += row.getCost_ms() == null ? 0 : row.getCost_ms();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((name, acc) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("count", acc[0]);
            item.put("avgCost", acc[0] == 0 ? 0 : acc[1] / acc[0]);
            result.add(item);
        });
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }

    /** 百炼用量：解析 log_content JSON，按 场景+模型 分组，输出 {scene, model, count, fail, inputTokens, outputTokens, avgCost} */
    private List<Map<String, Object>> aggregateModelCalls(List<AiBusinessLog> logs) {
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (AiBusinessLog row : logs) {
            JsonNode content = parseContent(row.getLog_content());
            String scene = row.getAgent_name() == null ? "未知场景" : row.getAgent_name();
            String model = content.path("model").asText("未知模型");
            Map<String, Object> acc = grouped.computeIfAbsent(scene + "|" + model, key -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("scene", scene);
                item.put("model", model);
                item.put("count", 0L);
                item.put("fail", 0L);
                item.put("inputTokens", 0L);
                item.put("outputTokens", 0L);
                item.put("costSum", 0L);
                return item;
            });
            acc.put("count", (Long) acc.get("count") + 1);
            if (!content.path("ok").asBoolean(true)) {
                acc.put("fail", (Long) acc.get("fail") + 1);
            }
            acc.put("inputTokens", (Long) acc.get("inputTokens") + content.path("in").asLong(0));
            acc.put("outputTokens", (Long) acc.get("outputTokens") + content.path("out").asLong(0));
            acc.put("costSum", (Long) acc.get("costSum") + (row.getCost_ms() == null ? 0 : row.getCost_ms()));
        }
        List<Map<String, Object>> result = new ArrayList<>(grouped.values());
        for (Map<String, Object> item : result) {
            long count = (Long) item.get("count");
            item.put("avgCost", count == 0 ? 0 : (Long) item.get("costSum") / count);
            item.remove("costSum");
        }
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }

    /** 最近调用记录：两类日志合并按 id 倒序取前 N 条，精简为列表展示字段 */
    private List<Map<String, Object>> recentLogs(List<AiBusinessLog> apiLogs, List<AiBusinessLog> modelLogs) {
        List<AiBusinessLog> merged = new ArrayList<>(apiLogs.size() + modelLogs.size());
        merged.addAll(apiLogs);
        merged.addAll(modelLogs);
        merged.sort((a, b) -> Long.compare(b.getId() == null ? 0 : b.getId(), a.getId() == null ? 0 : a.getId()));

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiBusinessLog row : merged) {
            if (result.size() >= RECENT_LIMIT) {
                break;
            }
            JsonNode content = parseContent(row.getLog_content());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", row.getLog_type());
            item.put("agent", row.getAgent_name());
            item.put("model", CallStatisticsService.TYPE_MODEL_CALL.equals(row.getLog_type())
                    ? content.path("model").asText("") : "");
            item.put("success", content.path("ok").asBoolean(
                    content.path("status").asInt(200) < 400));
            item.put("costMs", row.getCost_ms());
            item.put("createTime", row.getCreate_time() == null ? "" : fmt.format(row.getCreate_time()));
            result.add(item);
        }
        return result;
    }

    /** log_content 容错解析；空值或非法 JSON 返回空对象节点，不影响整体统计 */
    private JsonNode parseContent(String raw) {
        JsonNode node = AiJson.parseTree(raw);
        return node == null || !node.isObject() ? AiJson.mapper().createObjectNode() : node;
    }
}
