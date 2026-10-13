package com.example.aicodehelper.mapper;

import com.example.aicodehelper.domain.AiChatMessageAttachment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Jim
* @description 针对表【ai_chat_message_attachment(AI聊天消息附件表，综合问答图片/文档的MinIO对象存储记录)】的数据库操作Mapper
* @Entity com.example.aicodehelper.domain.AiChatMessageAttachment
*/
public interface AiChatMessageAttachmentMapper extends BaseMapper<AiChatMessageAttachment> {

}
