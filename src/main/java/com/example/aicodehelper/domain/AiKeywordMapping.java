package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 关键词术语映射表
 * @TableName ai_keyword_mapping
 */
@TableName(value ="ai_keyword_mapping")
@Data
public class AiKeywordMapping implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 原始词（用户口语）
     */
    @TableField(value = "original_word")
    private String original_word;

    /**
     * 标准术语
     */
    @TableField(value = "standard_word")
    private String standard_word;

    /**
     * 分类：面料/规格/检测
     */
    @TableField(value = "category")
    private String category;

    /**
     * 命中次数，统计用
     */
    @TableField(value = "hit_count")
    private Long hit_count;

    /**
     * 是否启用
     */
    @TableField(value = "enable")
    private Integer enable;

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
     * 逻辑删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
