package com.example.textileMultiModalAgent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 布号主数据表（ERP 面料成本，智能报价用）
 * @TableName ai_fabric_item
 */
@TableName(value ="ai_fabric_item")
@Data
public class AiFabricItem implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 布号（ERP编号，如 CW-2101）
     */
    @TableField(value = "fabric_code")
    private String fabric_code;

    /**
     * 面料名称（中文）
     */
    @TableField(value = "fabric_name")
    private String fabric_name;

    /**
     * 面料名称（英文）
     */
    @TableField(value = "fabric_name_en")
    private String fabric_name_en;

    /**
     * 成分（如 100% Cotton）
     */
    @TableField(value = "composition")
    private String composition;

    /**
     * 规格（纱支/密度，如 40x40 133x72）
     */
    @TableField(value = "spec")
    private String spec;

    /**
     * 克重（如 120 GSM）
     */
    @TableField(value = "weight")
    private String weight;

    /**
     * 门幅（如 57/58"）
     */
    @TableField(value = "width")
    private String width;

    /**
     * 坯布成本（元/米）
     */
    @TableField(value = "cost_material")
    private BigDecimal cost_material;

    /**
     * 织造/工艺成本（元/米）
     */
    @TableField(value = "cost_process")
    private BigDecimal cost_process;

    /**
     * 染整成本（元/米）
     */
    @TableField(value = "cost_dyeing")
    private BigDecimal cost_dyeing;

    /**
     * 检测成本（元/米）
     */
    @TableField(value = "cost_test")
    private BigDecimal cost_test;

    /**
     * 包装成本（元/米）
     */
    @TableField(value = "cost_packing")
    private BigDecimal cost_packing;

    /**
     * 交货期（确认订单后天数）
     */
    @TableField(value = "lead_time")
    private Integer lead_time;

    /**
     * 最小起订量 MOQ（米）
     */
    @TableField(value = "moq")
    private Integer moq;

    /**
     * 状态：0-下架 1-上架
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date update_time;

    /**
     * 逻辑删除：0-否 1-是
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
