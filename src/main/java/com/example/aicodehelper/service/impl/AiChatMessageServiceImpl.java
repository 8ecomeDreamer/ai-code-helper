package com.example.aicodehelper.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiChatMessage;
import com.example.aicodehelper.service.AiChatMessageService;
import com.example.aicodehelper.mapper.AiChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【ai_chat_message(AI对话消息明细表，保存每一轮user/assistant真实对话)】的数据库操作Service实现
* @createDate 2026-10-07 15:59:19
*/
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage>
    implements AiChatMessageService{

}




