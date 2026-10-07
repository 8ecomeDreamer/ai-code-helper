package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI对话会话主表
 * @TableName ai_chat_session
 */
@TableName(value ="ai_chat_session")
@Data
public class AiChatSession implements Serializable {
    /**
     * 会话主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话标题，一般取第一条用户提问
     */
    @TableField(value = "session_title")
    private String session_title;

    /**
     * 聊天用户ID（后台用户/访客ID）
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 使用的提示词模板id，关联ai_prompt_template
     */
    @TableField(value = "prompt_template_id")
    private Long prompt_template_id;

    /**
     * 本次会话使用模型id，关联ai_model
     */
    @TableField(value = "model_id")
    private Long model_id;

    /**
     * 会话状态 1正常 0关闭
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

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