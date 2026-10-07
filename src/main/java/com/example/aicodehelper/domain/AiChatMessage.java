package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI对话消息明细表，保存每一轮user/assistant真实对话
 * @TableName ai_chat_message
 */
@TableName(value ="ai_chat_message")
@Data
public class AiChatMessage implements Serializable {
    /**
     * 消息主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联ai_chat_session会话ID
     */
    @TableField(value = "session_id")
    private Long session_id;

    /**
     * 消息角色：user / assistant
     */
    @TableField(value = "role")
    private String role;

    /**
     * 消息文本内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 多模态数据：图片、语音等元数据JSON
     */
    @TableField(value = "multi_modal_data")
    private Object multi_modal_data;

    /**
     * Agent工具调用记录JSON
     */
    @TableField(value = "tool_calls")
    private Object tool_calls;

    /**
     * 本次召回的RAG分片片段JSON，用于溯源
     */
    @TableField(value = "rag_retrieve_chunk")
    private Object rag_retrieve_chunk;

    /**
     * 输入token
     */
    @TableField(value = "token_input")
    private Integer token_input;

    /**
     * 输出token
     */
    @TableField(value = "token_output")
    private Integer token_output;

    /**
     * 消息产生时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}