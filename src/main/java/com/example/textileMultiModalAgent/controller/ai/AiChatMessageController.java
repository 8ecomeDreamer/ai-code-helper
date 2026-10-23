package com.example.textileMultiModalAgent.controller.ai;

import com.example.textileMultiModalAgent.domain.AiChatMessage;
import com.example.textileMultiModalAgent.domain.vo.ChatMessageVo;
import com.example.textileMultiModalAgent.service.ai.AiChatMessageService;
import com.example.textileMultiModalAgent.service.ai.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天消息管理", description = "用于管理 AI 聊天中的消息记录")
@RestController
@RequestMapping("/chat-message")
public class AiChatMessageController {

    @Autowired
    private AiChatMessageService aiChatMessageService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    /**
     * 获取所有消息
     */
    @Operation(summary = "获取所有聊天消息")
    @GetMapping
    public List<AiChatMessage> getAllMessages() {
        return aiChatMessageService.list();
    }

    /**
     * 根据 ID 获取单条消息
     */
    @Operation(summary = "根据 ID 获取消息")
    @Parameter(name = "id", description = "消息的唯一标识", required = true)
    @GetMapping("/{id}")
    public AiChatMessage getMessageById(@PathVariable Long id) {
        return aiChatMessageService.getById(id);
    }

    /**
     * 添加新消息
     */
    @Operation(summary = "添加一条新消息")
    @PostMapping
    public AiChatMessage addMessage(@RequestBody AiChatMessage message) {
        aiChatMessageService.save(message);
        return message;
    }

    /**
     * 更新指定 ID 的消息
     */
    @Operation(summary = "更新消息内容")
    @PutMapping("/{id}")
    public AiChatMessage updateMessage(
            @PathVariable Long id,
            @RequestBody AiChatMessage updatedMessage) {
        updatedMessage.setId(id);
        aiChatMessageService.updateById(updatedMessage);
        return updatedMessage;
    }

    /**
     * 删除指定 ID 的消息
     */
    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public boolean deleteMessage(@PathVariable Long id) {
        return aiChatMessageService.removeById(id);
    }

    /**
     * 会话历史消息（含附件与预览链接，聊天页恢复历史对话用）
     */
    @Operation(summary = "根据会话 ID 获取历史消息（带附件）")
    @GetMapping("/history/{sessionId}")
    public List<ChatMessageVo> getHistory(@PathVariable Long sessionId) {
        return chatHistoryService.history(sessionId);
    }

    /**
     * 根据会话 ID 查询消息列表
     */
    @Operation(summary = "根据会话 ID 获取消息列表")
    @GetMapping("/session/{chatSessionId}")
    public List<AiChatMessage> getMessagesBySessionId(@PathVariable Long chatSessionId) {
        return aiChatMessageService.lambdaQuery()
                .eq(AiChatMessage::getSession_id, chatSessionId)
                .orderByAsc(AiChatMessage::getId)
                .list();
    }
}
