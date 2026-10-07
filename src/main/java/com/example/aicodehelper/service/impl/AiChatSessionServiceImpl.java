package com.example.aicodehelper.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiChatSession;
import com.example.aicodehelper.service.AiChatSessionService;
import com.example.aicodehelper.mapper.AiChatSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【ai_chat_session(AI对话会话主表)】的数据库操作Service实现
* @createDate 2026-10-07 15:59:19
*/
@Service
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession>
    implements AiChatSessionService{

}




