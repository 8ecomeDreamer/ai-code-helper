package com.example.textileMultiModalAgent.service.admin;

import com.example.textileMultiModalAgent.common.core.AiJson;
import com.example.textileMultiModalAgent.domain.AiBusinessLog;
import com.example.textileMultiModalAgent.service.ai.AiBusinessLogService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 按用户的 token 日预算限额服务。
 *
 * <p>统计口径：汇总当天（自然日）该用户全部 {@code model_call} 日志（ai_business_log，
 * log_content.in + log_content.out）的输入+输出 token 之和，与配置预算比较。
 * 综合问答文档/图片轮（多模态）单次消耗巨大，是线上重点防护对象，故问答入口据此拦截。</p>
 *
 * <p>归属来源：多模态/面料/语音经显式参数写入 user_id，可靠；文本 AiServices 链路经
 * {@code ModelCallContext} 尽力回填。匿名（未登录）无法可靠归属，默认放行不受限——
 * 上线应在网关/登录层约束匿名访问。</p>
 *
 * <p>预算经 {@code textile.quota.daily-token-budget} 配置，默认 200000；置为 0 或负数表示不限额。</p>
 */
@Service
@Slf4j
public class QuotaService {

    /** 每用户每日 token 预算（输入+输出合计）；<=0 表示不限额 */
    @Value("${textile.quota.daily-token-budget:200000}")
    private long dailyTokenBudget;

    @Resource
    private AiBusinessLogService aiBusinessLogService;

    /** 当前配置的每日 token 预算（供提示语展示） */
    public long dailyTokenBudget() {
        return dailyTokenBudget;
    }

    /** 统计某用户当天已累计消耗的 token（model_call 的 in+out 之和）；匿名返回 0 */
    public long usedTokensToday(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Date dayStart = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<AiBusinessLog> rows = aiBusinessLogService.lambdaQuery()
                .eq(AiBusinessLog::getUser_id, userId)
                .eq(AiBusinessLog::getLog_type, CallStatisticsService.TYPE_MODEL_CALL)
                .ge(AiBusinessLog::getCreate_time, dayStart)
                .list();
        long total = 0L;
        for (AiBusinessLog row : rows) {
            total += tokensOf(row.getLog_content());
        }
        return total;
    }

    /** 是否已超出当日预算（匿名或不限额时恒 false） */
    public boolean overBudget(Long userId) {
        if (userId == null || dailyTokenBudget <= 0) {
            return false;
        }
        return usedTokensToday(userId) >= dailyTokenBudget;
    }

    /** 从 model_call 的 log_content JSON 中解析 in+out token 之和 */
    private long tokensOf(String logContent) {
        if (logContent == null || logContent.isBlank()) {
            return 0L;
        }
        JsonNode node = AiJson.parseTree(logContent);
        if (node == null) {
            return 0L;
        }
        return node.path("in").asLong(0L) + node.path("out").asLong(0L);
    }
}
