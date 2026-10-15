package com.example.aicodehelper.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 聊天历史消息 VO：ai_chat_message + 附件（含 MinIO 临时预览链接），
 * 供前端恢复历史会话时渲染文本/图片/文档附件。
 */
@Data
public class ChatMessageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息主键 */
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 消息角色：user / assistant */
    private String role;

    /** 消息文本内容 */
    private String content;

    /** 消息产生时间 */
    private Date createTime;

    /** 附件列表（无附件为空列表） */
    private List<Attachment> attachments;

    /** 消息附件（图片/文档） */
    @Data
    public static class Attachment implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 附件类型：image / file */
        private String type;

        /** 原始文件名 */
        private String fileName;

        /** MinIO 对象名 */
        private String objectName;

        /** MIME 类型 */
        private String mimeType;

        /** 文件大小（字节） */
        private Long fileSize;

        /** 临时预览/下载链接（有效期 30 分钟） */
        private String url;
    }
}
