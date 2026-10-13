package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.biz.ResearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 竞品调研接口。
 * 映射路径 /research，完整 URL /api/research（context-path=/api）。
 */
@Tag(name = "竞品调研", description = "Agent 调用联网搜索 MCP 聚合面料行情")
@RestController
@RequestMapping("/research")
public class ResearchController {

    @Resource
    private ResearchService researchService;

    /**
     * 按面料关键词调研竞品行情。
     *
     * @param keyword 面料关键词，如：全棉府绸 / 涤棉纱卡
     * @return data 为 {fabric, competitors[], analysis[], demo}
     */
    @Operation(summary = "面料竞品行情调研")
    @GetMapping("/fabric")
    public Result research(@RequestParam("keyword") String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Result.error("面料关键词不能为空");
        }
        try {
            return Result.success(researchService.research(keyword));
        } catch (Exception e) {
            return Result.error("竞品调研失败：" + e.getMessage());
        }
    }
}
