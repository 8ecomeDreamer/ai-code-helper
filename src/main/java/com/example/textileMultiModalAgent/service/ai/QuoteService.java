package com.example.textileMultiModalAgent.service.ai;

import com.example.textileMultiModalAgent.ai.agent.QuotationAgent;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.belowMoq;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.fabricOf;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.groupInt;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.intText;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.money;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.num;
import static com.example.textileMultiModalAgent.ai.agent.QuotationAgent.str;

/**
 * 智能报价邮件生成服务（应用编排层）。
 *
 * <p>报价数字由前端基于 ERP 布号表成本确定性计算后传入；prompt 与模型 I/O 在
 * {@link QuotationAgent}，本服务只负责降级决策：模型不可用或返回空时降级本地模板，
 * 保证接口始终可用。</p>
 */
@Slf4j
@Service
public class QuoteService {

    @Resource
    private QuotationAgent quotationAgent;

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
        String email = quotationAgent.writeEmail(quote, lang, customer);
        if (email == null) {
            log.warn("报价邮件模型不可用或返回空，降级本地模板");
            return fallback;
        }
        return email;
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
                "**主题：" + str(fabric, "name") + " 报价单（" + str(quote, "tradeTerm") + ")**",
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
}
