package com.example.aicodehelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicodehelper.domain.AiPromptTemplate;
import com.example.aicodehelper.service.AiPromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "提示模板管理", description = "管理 AI 使用的提示模板")
@RestController
@RequestMapping("/api/prompt-template")
public class AiPromptTemplateController {

    @Autowired
    private AiPromptTemplateService aiPromptTemplateService;

    @Operation(summary = "获取所有模板")
    @GetMapping
    public List<AiPromptTemplate> getAllTemplates() {
        return aiPromptTemplateService.list();
    }

    @Operation(summary = "根据名称模糊查询模板")
    @GetMapping("/search")
    public List<AiPromptTemplate> searchTemplates(@RequestParam String name) {
        QueryWrapper<AiPromptTemplate> sql = new QueryWrapper<>();
        sql.like("template_name", name);
        return aiPromptTemplateService.getBaseMapper().selectList(sql);
    }


    @Operation(summary = "添加新模板")
    @PostMapping
    public boolean addTemplate(@RequestBody AiPromptTemplate template) {
        return aiPromptTemplateService.save(template);
    }
}
