package com.example.aicodehelper.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.aicodehelper.domain.AiChatMessage;

/**
* @author Jim
* @description 针对表【ai_chat_message(AI对话消息明细表，保存每一轮user/assistant真实对话)】的数据库操作Service
* @createDate 2026-10-07 15:59:19
*/
public interface AiChatMessageService extends IService<AiChatMessage> {

}
