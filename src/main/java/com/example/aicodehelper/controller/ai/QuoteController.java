package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.ai.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能报价接口。
 * 映射路径 /quote，完整 URL /api/quote（context-path=/api）。
 */
@Tag(name = "智能报价", description = "基于布号表 ERP 成本由大模型生成多语言报价邮件")
@RestController
@RequestMapping("/quote")
public class QuoteController {

    @Resource
    private QuoteService quoteService;

    /**
     * 生成报价邮件（大模型撰写，数字强约束不改；模型不可用降级本地模板）。
     *
     * <p>请求体：{@code { "quote": {...前端 computeQuote 结果...}, "lang": "zh|en|both", "customer": "称呼" }}</p>
     *
     * @return data 为邮件 Markdown 文本
     */
    @Operation(summary = "生成报价邮件")
    @PostMapping("/email")
    @SuppressWarnings("unchecked")
    public Result generateEmail(@RequestBody Map<String, Object> body) {
        Object quoteObj = body.get("quote");
        if (!(quoteObj instanceof Map)) {
            return Result.error("报价数据 quote 不能为空");
        }
        String lang = String.valueOf(body.getOrDefault("lang", "both"));
        String customer = body.get("customer") == null ? "" : String.valueOf(body.get("customer"));
        try {
            String email = quoteService.generateEmail((Map<String, Object>) quoteObj, lang, customer);
            return Result.success("报价邮件已生成", email);
        } catch (Exception e) {
            return Result.error("报价邮件生成失败：" + e.getMessage());
        }
    }
}
