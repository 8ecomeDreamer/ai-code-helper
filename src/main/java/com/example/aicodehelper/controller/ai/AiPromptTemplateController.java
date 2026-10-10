package com.example.aicodehelper.controller.ai;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicodehelper.domain.AiPromptTemplate;
import com.example.aicodehelper.service.ai.AiPromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "提示模板管理", description = "管理 AI 使用的提示模板")
@RestController
@RequestMapping("/prompt-template")
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

    @Operation(summary = "根据 ID 获取模板")
    @GetMapping("/{id}")
    public AiPromptTemplate getTemplateById(@PathVariable Long id) {
        return aiPromptTemplateService.getById(id);
    }

    @Operation(summary = "修改模板")
    @PutMapping
    public boolean updateTemplate(@RequestBody AiPromptTemplate template) {
        return aiPromptTemplateService.updateById(template);
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public boolean deleteTemplate(@PathVariable Long id) {
        return aiPromptTemplateService.removeById(id);
    }
}
