package com.example.aicodehelper.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.domain.AiModel;
import com.example.aicodehelper.service.ai.AiModelService;
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
 * AI 大模型配置管理接口
 * 映射路径 /agent/model，完整 URL /api/agent/model（context-path=/api）
 */
@Tag(name = "模型配置管理", description = "大模型接入参数与启用状态")
@RestController
@RequestMapping("/agent/model")
public class AiModelController {

    @Resource
    private AiModelService aiModelService;

    /**
     * 列表：支持按名称/编码模糊、状态过滤
     */
    @Operation(summary = "模型配置列表")
    @RequiresPermissions("agent:model:list")
    @GetMapping("/list")
    public Result list(AiModel query) {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getModel_name())) {
                wrapper.like(AiModel::getModel_name, query.getModel_name());
            }
            if (StringUtils.hasText(query.getModel_code())) {
                wrapper.like(AiModel::getModel_code, query.getModel_code());
            }
            if (query.getStatus() != null) {
                wrapper.eq(AiModel::getStatus, query.getStatus());
            }
        }
        wrapper.orderByDesc(AiModel::getId);
        List<AiModel> list = aiModelService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 详情
     */
    @Operation(summary = "模型配置详情")
    @RequiresPermissions("agent:model:query")
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Long id) {
        return Result.success(aiModelService.getById(id));
    }

    /**
     * 新增
     */
    @Operation(summary = "新增模型配置")
    @RequiresPermissions("agent:model:add")
    @PostMapping
    public Result add(@RequestBody AiModel model) {
        if (!StringUtils.hasText(model.getModel_name())
                || !StringUtils.hasText(model.getModel_code())) {
            return Result.error("模型名称与编码不能为空");
        }
        if (model.getStatus() == null) {
            model.setStatus(1);
        }
        model.setCreate_time(new Date());
        model.setUpdate_time(new Date());
        return aiModelService.save(model) ? Result.success() : Result.error();
    }

    /**
     * 修改
     */
    @Operation(summary = "修改模型配置")
    @RequiresPermissions("agent:model:edit")
    @PutMapping
    public Result edit(@RequestBody AiModel model) {
        if (model.getId() == null) {
            return Result.error("修改失败，主键不能为空");
        }
        model.setUpdate_time(new Date());
        return aiModelService.updateById(model) ? Result.success() : Result.error();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除模型配置")
    @RequiresPermissions("agent:model:remove")
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Long id) {
        return aiModelService.removeById(id) ? Result.success() : Result.error();
    }
}
