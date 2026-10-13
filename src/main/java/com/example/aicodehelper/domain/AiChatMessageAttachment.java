package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * AI聊天消息附件表（综合问答图片/文档的MinIO对象存储记录）
 * @TableName ai_chat_message_attachment
 */
@TableName(value ="ai_chat_message_attachment")
@Data
public class AiChatMessageAttachment implements Serializable {
    /**
     * 附件主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息ID ai_chat_message.id
     */
    @TableField(value = "message_id")
    private Long message_id;

    /**
     * 会话ID ai_chat_session.id（同时作为MinIO文件夹名）
     */
    @TableField(value = "session_id")
    private Long session_id;

    /**
     * 附件类型：image=图片 file=文档附件
     */
    @TableField(value = "attach_type")
    private String attach_type;

    /**
     * 原始文件名
     */
    @TableField(value = "file_name")
    private String file_name;

    /**
     * 文件扩展名（不含点，如 jpg、pdf）
     */
    @TableField(value = "file_ext")
    private String file_ext;

    /**
     * MIME类型，如 image/jpeg
     */
    @TableField(value = "mime_type")
    private String mime_type;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long file_size;

    /**
     * MinIO bucket名
     */
    @TableField(value = "bucket_name")
    private String bucket_name;

    /**
     * MinIO对象名（含会话ID文件夹，如 12/uuid.jpg）
     */
    @TableField(value = "object_name")
    private String object_name;

    /**
     * 同消息内展示顺序（上传顺序）
     */
    @TableField(value = "sort_no")
    private Integer sort_no;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
