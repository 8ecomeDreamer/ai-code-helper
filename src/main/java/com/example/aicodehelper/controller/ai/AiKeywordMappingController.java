package com.example.aicodehelper.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.domain.AiKeywordMapping;
import com.example.aicodehelper.service.ai.AiKeywordMappingService;
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
 * 关键词术语映射管理接口（口语词 -> 标准术语）
 * 映射路径 /agent/keyword，完整 URL /api/agent/keyword（context-path=/api）
 */
@Tag(name = "关键词映射管理", description = "口语词到标准术语的查询改写映射")
@RestController
@RequestMapping("/agent/keyword")
public class AiKeywordMappingController {

    @Resource
    private AiKeywordMappingService aiKeywordMappingService;

    /**
     * 列表：支持按原始词/标准词模糊、分类、启用状态过滤
     */
    @Operation(summary = "关键词映射列表")
    @RequiresPermissions("agent:keyword:list")
    @GetMapping("/list")
    public Result list(AiKeywordMapping query) {
        LambdaQueryWrapper<AiKeywordMapping> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (StringUtils.hasText(query.getOriginal_word())) {
                wrapper.like(AiKeywordMapping::getOriginal_word, query.getOriginal_word());
            }
            if (StringUtils.hasText(query.getStandard_word())) {
                wrapper.like(AiKeywordMapping::getStandard_word, query.getStandard_word());
            }
            if (StringUtils.hasText(query.getCategory())) {
                wrapper.eq(AiKeywordMapping::getCategory, query.getCategory());
            }
            if (query.getEnable() != null) {
                wrapper.eq(AiKeywordMapping::getEnable, query.getEnable());
            }
        }
        wrapper.orderByDesc(AiKeywordMapping::getId);
        List<AiKeywordMapping> list = aiKeywordMappingService.list(wrapper);
        return Result.success(list);
    }

    /**
     * 详情
     */
    @Operation(summary = "关键词映射详情")
    @RequiresPermissions("agent:keyword:query")
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Long id) {
        return Result.success(aiKeywordMappingService.getById(id));
    }

    /**
     * 新增
     */
    @Operation(summary = "新增关键词映射")
    @RequiresPermissions("agent:keyword:add")
    @PostMapping
    public Result add(@RequestBody AiKeywordMapping keyword) {
        if (!StringUtils.hasText(keyword.getOriginal_word())
                || !StringUtils.hasText(keyword.getStandard_word())) {
            return Result.error("原始词与标准词不能为空");
        }
        if (keyword.getHit_count() == null) {
            keyword.setHit_count(0L);
        }
        if (keyword.getEnable() == null) {
            keyword.setEnable(1);
        }
        keyword.setCreate_time(new Date());
        keyword.setUpdate_time(new Date());
        return aiKeywordMappingService.save(keyword) ? Result.success() : Result.error();
    }

    /**
     * 修改
     */
    @Operation(summary = "修改关键词映射")
    @RequiresPermissions("agent:keyword:edit")
    @PutMapping
    public Result edit(@RequestBody AiKeywordMapping keyword) {
        if (keyword.getId() == null) {
            return Result.error("修改失败，主键不能为空");
        }
        keyword.setUpdate_time(new Date());
        return aiKeywordMappingService.updateById(keyword) ? Result.success() : Result.error();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除关键词映射")
    @RequiresPermissions("agent:keyword:remove")
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable Long id) {
        return aiKeywordMappingService.removeById(id) ? Result.success() : Result.error();
    }
}
