package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI提示词模板表，后台管理，不存储实时用户对话
 * @TableName ai_prompt_template
 */
@TableName(value ="ai_prompt_template")
@Data
public class AiPromptTemplate implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField(value = "template_name")
    private String template_name;

    /**
     * 对应agent
     */
    @TableField(value = "agent_name")
    private String agent_name;

    /**
     * 模板类型: system / user
     */
    @TableField(value = "prompt_type")
    private String prompt_type;

    /**
     * 提示词内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 关联ai_model主键
     */
    @TableField(value = "model_id")
    private Long model_id;

    /**
     * 是否启用
     */
    @TableField(value = "enable")
    private Integer enable;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 版本号，乐观锁，编辑自动+1
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 创建人ID
     */
    @TableField(value = "creator_id")
    private Long creator_id;

    /**
     * 创建人
     */
    @TableField(value = "creator_name")
    private String creator_name;

    /**
     * 更新人ID
     */
    @TableField(value = "updater_id")
    private Long updater_id;

    /**
     * 更新人
     */
    @TableField(value = "updater_name")
    private String updater_name;

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