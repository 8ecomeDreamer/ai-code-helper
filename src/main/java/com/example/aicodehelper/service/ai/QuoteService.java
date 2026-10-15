package com.example.aicodehelper.service.ai;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 智能报价邮件生成服务。
 *
 * <p>报价数字由前端基于 ERP 布号表成本确定性计算后传入，本服务只负责「写邮件」：
 * 调 qwen 大模型按语言（zh/en/both）产出 Markdown 邮件，Prompt 强约束不得改动任何数字；
 * 模型不可用或返回空时降级为本地模板，保证接口始终可用。</p>
 */
@Slf4j
@Service
public class QuoteService {

    @Resource
    private ChatModel myQwenChatModel;

    /**
     * 生成报价邮件 Markdown。
     *
     * @param quote    前端 computeQuote 的完整结果（含 fabric 子对象与各项金额）
     * @param lang     zh / en / both
     * @param customer 客户称呼，可空
     * @return 邮件 Markdown 文本
     */
    public String generateEmail(Map<String, Object> quote, String lang, String customer) {
        String fallback = fallbackEmail(quote, lang, customer);
        try {
            String email = myQwenChatModel.chat(buildPrompt(quote, lang, customer));
            if (email == null || email.isBlank()) {
                log.warn("报价邮件模型返回空，降级本地模板");
                return fallback;
            }
            return email.trim();
        } catch (Exception e) {
            log.warn("报价邮件模型调用失败，降级本地模板：{}", e.getMessage());
            return fallback;
        }
    }

    /* ---------------- Prompt 构建 ---------------- */

    private String buildPrompt(Map<String, Object> quote, String lang, String customer) {
        Map<String, Object> fabric = fabricOf(quote);
        String langRule = switch (lang == null ? "both" : lang) {
            case "zh" -> "仅输出中文邮件";
            case "en" -> "仅输出英文邮件";
            default -> "输出中英双语邮件：中文在前、英文在后，中间用一行 --- 分隔";
        };
        String customerName = customer == null || customer.isBlank() ? "" : customer.trim();
        return """
                你是纺织外贸公司的资深业务员，请根据下面的报价数据撰写一封报价邮件，直接输出 Markdown 正文，不要任何解释或代码块。
                语言要求：%s。
                硬约束：邮件中的一切数字（单价、总金额、数量、折扣率、交货期、MOQ）必须与数据逐字一致，禁止重新计算、禁止修改、禁止编造任何金额与日期。
                邮件结构：主题行；称呼（客户称呼为空时用「尊敬的客户」/「Dear Customer」）；开场致谢；报价明细 Markdown 表格（品名/规格/克重门幅/数量/单价含贸易术语/总金额/交货期/付款方式：30%% 预付款，发货前付清尾款）；如折扣率大于 0 需注明数量折扣，如低于 MOQ 需提示成交价可能上浮 5%%-8%%；报价有效期 15 天；结尾祝颂语与落款「纺织外贸销售部」/「Textile Export Sales Department」。
                报价数据：
                - 客户称呼：%s
                - 布号：%s
                - 品名：%s（%s）
                - 规格：%s；克重/门幅：%s / %s
                - 数量：%s 米
                - 贸易术语：%s；结算币种符号：%s
                - 单价：%s/米；总金额：%s
                - 利润率：%s%%；数量折扣：%s%%
                - 交货期：%s 天；MOQ：%s 米；是否低于MOQ：%s
                """.formatted(langRule, customerName,
                str(fabric, "code"), str(fabric, "name"), str(fabric, "nameEn"),
                str(fabric, "spec"), str(fabric, "weight"), str(fabric, "width"),
                intText(quote, "quantity"), str(quote, "tradeTerm"), str(quote, "symbol"),
                money(num(quote, "unitPrice"), str(quote, "symbol")),
                money(num(quote, "totalAmount"), str(quote, "symbol")),
                intText(quote, "marginPct"), intText(quote, "discountPct"),
                intText(quote, "leadTime"), intText(quote, "moq"),
                Boolean.TRUE.equals(quote.get("belowMoq")) ? "是" : "否");
    }

    /* ---------------- 本地模板兜底（与前端 quote.js 模板同构） ---------------- */

    private String fallbackEmail(Map<String, Object> quote, String lang, String customer) {
        String name = customer == null ? "" : customer.trim();
        String language = lang == null ? "zh" : lang;
        if ("en".equals(language)) return enEmail(quote, name);
        if ("both".equals(language)) return zhEmail(quote, name) + "\n\n---\n\n" + enEmail(quote, name);
        return zhEmail(quote, name);
    }

    private String zhEmail(Map<String, Object> quote, String customer) {
        Map<String, Object> fabric = fabricOf(quote);
        List<String> lines = List.of(
                "**主题：" + str(fabric, "name") + " 报价单（" + str(quote, "tradeTerm") + "）**",
                "",
                "尊敬的 " + (customer.isEmpty() ? "客户" : customer) + "：",
                "",
                "您好！感谢您对我司产品的关注，现将您询价的面料报价如下：",
                "",
                "| 项目 | 内容 |",
                "| --- | --- |",
                "| 品名 | " + str(fabric, "name") + "（" + str(fabric, "nameEn") + "） |",
                "| 规格 | " + str(fabric, "spec") + " |",
                "| 克重 / 幅宽 | " + str(fabric, "weight") + " / " + str(fabric, "width") + " |",
                "| 数量 | " + groupInt(quote, "quantity") + " 米 |",
                "| 单价 | " + money(num(quote, "unitPrice"), str(quote, "symbol")) + "/米（" + str(quote, "tradeTerm") + "） |",
                "| 总金额 | " + money(num(quote, "totalAmount"), str(quote, "symbol")) + " |",
                "| 交货期 | 确认订单后 " + intText(quote, "leadTime") + " 天 |",
                "| 付款方式 | 30% 预付款，发货前付清尾款 |",
                "",
                num(quote, "discountPct") > 0 ? "> 本单已享受大客户数量折扣 " + intText(quote, "discountPct") + "%。" : "",
                belowMoq(quote) ? "> 提示：起订量为 " + groupInt(quote, "moq") + " 米，当前数量低于起订量，成交价可能上浮 5%-8%。" : "",
                "",
                "以上报价有效期为 15 天。如需样品或进一步洽谈，请随时与我们联系。",
                "",
                "顺颂商祺！",
                "纺织外贸销售部");
        return String.join("\n", lines.stream().filter(line -> !line.isEmpty()).toList());
    }

    private String enEmail(Map<String, Object> quote, String customer) {
        Map<String, Object> fabric = fabricOf(quote);
        List<String> lines = List.of(
                "**Subject: Quotation for " + str(fabric, "nameEn") + " (" + str(quote, "tradeTerm") + ")**",
                "",
                "Dear " + (customer.isEmpty() ? "Customer" : customer) + ",",
                "",
                "Thank you for your inquiry. We are pleased to quote as follows:",
                "",
                "| Item | Details |",
                "| --- | --- |",
                "| Product | " + str(fabric, "nameEn") + " (" + str(fabric, "name") + ") |",
                "| Specification | " + str(fabric, "spec") + " |",
                "| Weight / Width | " + str(fabric, "weight") + " / " + str(fabric, "width") + " |",
                "| Quantity | " + groupInt(quote, "quantity") + " meters |",
                "| Unit Price | " + money(num(quote, "unitPrice"), str(quote, "symbol")) + "/m (" + str(quote, "tradeTerm") + ") |",
                "| Total Amount | " + money(num(quote, "totalAmount"), str(quote, "symbol")) + " |",
                "| Lead Time | " + intText(quote, "leadTime") + " days after order confirmation |",
                "| Payment Terms | 30% deposit, balance before shipment |",
                "",
                num(quote, "discountPct") > 0 ? "> A volume discount of " + intText(quote, "discountPct") + "% has been applied." : "",
                belowMoq(quote) ? "> Note: MOQ is " + groupInt(quote, "moq") + " meters. Orders below MOQ may be subject to a 5%-8% surcharge." : "",
                "",
                "This quotation is valid for 15 days. Samples are available upon request.",
                "",
                "Best regards,",
                "Textile Export Sales Department");
        return String.join("\n", lines.stream().filter(line -> !line.isEmpty()).toList());
    }

    /* ---------------- 取值小工具（前端传入为 JS 数字/嵌套 Map） ---------------- */

    @SuppressWarnings("unchecked")
    private Map<String, Object> fabricOf(Map<String, Object> quote) {
        Object fabric = quote.get("fabric");
        return fabric instanceof Map ? (Map<String, Object>) fabric : Map.of();
    }

    private String str(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private double num(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0D;
        }
    }

    private String intText(Map<String, Object> map, String key) {
        return String.valueOf(Math.round(num(map, key)));
    }

    private String groupInt(Map<String, Object> map, String key) {
        return String.format(Locale.US, "%,d", Math.round(num(map, key)));
    }

    private boolean belowMoq(Map<String, Object> quote) {
        return Boolean.TRUE.equals(quote.get("belowMoq"));
    }

    /** 金额格式：符号 + 千分位两位小数，与前端 formatMoney 一致 */
    private String money(double value, String symbol) {
        return symbol + String.format(Locale.US, "%,.2f", value);
    }
}
