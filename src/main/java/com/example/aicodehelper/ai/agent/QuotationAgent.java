package com.example.aicodehelper.ai.agent;

import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 智能报价 Agent：持有报价邮件 prompt 与模型 I/O。
 *
 * <p>报价数字由前端基于 ERP 布号表成本确定性计算后传入，本 Agent 只「写邮件」：
 * prompt 强约束一切数字逐字照抄、禁止重算；模型不可用/返回空时返回 null，
 * 由 QuoteService 降级本地模板。</p>
 */
@Slf4j
@Component
public class QuotationAgent extends BizAgent {

    /**
     * 生成报价邮件 Markdown；模型失败或返回空时返回 null（降级决策在 Service 层）。
     *
     * @param quote    前端 computeQuote 的完整结果（含 fabric 子对象与各项金额）
     * @param lang     zh / en / both
     * @param customer 客户称呼，可空
     */
    public String writeEmail(Map<String, Object> quote, String lang, String customer) {
        try {
            String email = chat(buildPrompt(quote, lang, customer));
            if (email == null || email.isBlank()) {
                log.warn("报价邮件模型返回空");
                return null;
            }
            return email.trim();
        } catch (Exception e) {
            log.warn("报价邮件模型调用失败：{}", e.getMessage());
            return null;
        }
    }

    /* ---------------- Prompt ---------------- */

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

    /* ---------------- 报价数据取值小工具（前端传入为 JS 数字/嵌套 Map；兜底模板共用） ---------------- */

    @SuppressWarnings("unchecked")
    public static Map<String, Object> fabricOf(Map<String, Object> quote) {
        Object fabric = quote.get("fabric");
        return fabric instanceof Map ? (Map<String, Object>) fabric : Map.of();
    }

    public static String str(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public static double num(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0D;
        }
    }

    public static String intText(Map<String, Object> map, String key) {
        return String.valueOf(Math.round(num(map, key)));
    }

    public static String groupInt(Map<String, Object> map, String key) {
        return String.format(Locale.US, "%,d", Math.round(num(map, key)));
    }

    public static boolean belowMoq(Map<String, Object> quote) {
        return Boolean.TRUE.equals(quote.get("belowMoq"));
    }

    /** 金额格式：符号 + 千分位两位小数，与前端 formatMoney 一致 */
    public static String money(double value, String symbol) {
        return symbol + String.format(Locale.US, "%,.2f", value);
    }
}
