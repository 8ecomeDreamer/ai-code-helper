package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI Agent业务执行日志
 * @TableName ai_business_log
 */
@TableName(value ="ai_business_log")
@Data
public class AiBusinessLog implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID ai_chat_session.id
     */
    @TableField(value = "session_id")
    private Long session_id;

    /**
     * 消息ID ai_chat_message.id
     */
    @TableField(value = "message_id")
    private Long message_id;

    /**
     * Agent名称：PlanAndExecuteAgent / ReActAgent / ReflectionAgent
     */
    @TableField(value = "agent_name")
    private String agent_name;

    /**
     * 日志类型：plan / tool_call / reflection / dispatch
     */
    @TableField(value = "log_type")
    private String log_type;

    /**
     * 日志详情
     */
    @TableField(value = "log_content")
    private String log_content;

    /**
     * 耗时毫秒
     */
    @TableField(value = "cost_ms")
    private Long cost_ms;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
