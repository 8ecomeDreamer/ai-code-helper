package com.example.aicodehelper.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.domain.AiBusinessLog;
import com.example.aicodehelper.service.ai.AiBusinessLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Agent 业务执行日志管理接口（链路追踪）
 * 映射路径 /agent/log，完整 URL /api/agent/log（context-path=/api）
 * 注意：ai_business_log 表无 update_time / deleted 列，删除为物理删除
 */
@Tag(name = "AI业务日志管理", description = "Agent 执行链路日志（plan/tool_call/reflection/dispatch）")
@RestController
@RequestMapping("/agent/log")
public class AiBusinessLogController {

    @Resource
    private AiBusinessLogService aiBusinessLogService;

    /**
     * 列表：支持按会话、Agent 名称、日志类型过滤
     */
    @Operation(summary = "业务日志列表")
    @RequiresPermissions("agent:log:list")
    @GetMapping("/list")
    public Result list(AiBusinessLog query) {
        LambdaQueryWrapper<AiBusinessLog> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (query.getSession_id() != null) {
                wrapper.eq(AiBusinessLog::getSession_id, query.getSession_id());
            }
            if (StringUtils.hasText(query.getAgent_name())) {
                wrapper.like(AiBusinessLog::getAgent_name, query.getAgent_name());
            }
            if (StringUtils.hasText(query.getLog_type())) {
                wrapper.eq(AiBusinessLog::getLog_type, query.getLog_type());
            }
        }
        wrapper.orderByDesc(AiBusinessLog::getId);
        List<AiBusinessLog> list = aiBusinessLogService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 详情
     */
    @Operation(summary = "业务日志详情")
    @RequiresPermissions("agent:log:query")
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Long id) {
        return Result.success(aiBusinessLogService.getById(id));
    }

    /**
     * 新增（一般由 Agent 执行链路内部写入，此处提供管理端补录能力）
     */
    @Operation(summary = "新增业务日志")
    @RequiresPermissions("agent:log:add")
    @PostMapping
    public Result add(@RequestBody AiBusinessLog log) {
        if (log.getCreate_time() == null) {
            log.setCreate_time(new Date());
        }
        return aiBusinessLogService.save(log) ? Result.success() : Result.error();
    }

    /**
     * 修改
     */
    @Operation(summary = "修改业务日志")
    @RequiresPermissions("agent:log:edit")
    @PutMapping
    public Result edit(@RequestBody AiBusinessLog log) {
        if (log.getId() == null) {
            return Result.error("修改失败，主键不能为空");
        }
        return aiBusinessLogService.updateById(log) ? Result.success() : Result.error();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除业务日志")
    @RequiresPermissions("agent:log:remove")
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Long id) {
        return aiBusinessLogService.removeById(id) ? Result.success() : Result.error();
    }
}
