package com.example.textileMultiModalAgent.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 面料布号 VO：智能报价下拉与报价计算的前端契约
 *
 * <p>字段形状与前端 utils/quote.js computeQuote 的入参保持一致：
 * code/name/nameEn/spec/weight/width/cost{material,process,dyeing,test,packing}/leadTime/moq。</p>
 */
@Data
public class FabricItemVo implements Serializable {

    /** 布号（ERP 编号） */
    private String code;

    /** 面料名称（中文） */
    private String name;

    /** 面料名称（英文） */
    private String nameEn;

    /** 成分 */
    private String composition;

    /** 规格（纱支/密度） */
    private String spec;

    /** 克重 */
    private String weight;

    /** 门幅 */
    private String width;

    /** 成本五项拆分（元/米） */
    private Cost cost;

    /** 交货期（天） */
    private Integer leadTime;

    /** 最小起订量（米） */
    private Integer moq;

    /**
     * ERP 成本五项拆分
     */
    @Data
    public static class Cost implements Serializable {
        /** 坯布成本 */
        private BigDecimal material;
        /** 织造/工艺成本 */
        private BigDecimal process;
        /** 染整成本 */
        private BigDecimal dyeing;
        /** 检测成本 */
        private BigDecimal test;
        /** 包装成本 */
        private BigDecimal packing;

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
