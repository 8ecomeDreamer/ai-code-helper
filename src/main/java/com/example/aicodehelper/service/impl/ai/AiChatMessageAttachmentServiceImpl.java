package com.example.aicodehelper.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiChatMessageAttachment;
import com.example.aicodehelper.service.ai.AiChatMessageAttachmentService;
import com.example.aicodehelper.mapper.AiChatMessageAttachmentMapper;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【ai_chat_message_attachment(AI聊天消息附件表，综合问答图片/文档的MinIO对象存储记录)】的数据库操作Service实现
*/
@Service
public class AiChatMessageAttachmentServiceImpl extends ServiceImpl<AiChatMessageAttachmentMapper, AiChatMessageAttachment>
    implements AiChatMessageAttachmentService {

}
