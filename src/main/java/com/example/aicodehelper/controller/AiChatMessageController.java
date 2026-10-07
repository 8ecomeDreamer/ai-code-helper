package com.example.aicodehelper.controller;

import com.example.aicodehelper.domain.AiChatMessage;
import com.example.aicodehelper.service.AiChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天消息管理", description = "用于管理 AI 聊天中的消息记录")
@RestController
@RequestMapping("/api/chat-message")
public class AiChatMessageController {

    @Autowired
    private AiChatMessageService aiChatMessageService;

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
     * 根据会话 ID 查询消息列表
     */
    @Operation(summary = "根据会话 ID 获取消息列表")
    @GetMapping("/session/{chatSessionId}")
    public List<AiChatMessage> getMessagesBySessionId(@PathVariable Long chatSessionId) {
        return aiChatMessageService.lambdaQuery()
                .eq(AiChatMessage::getId, chatSessionId)
                .list();
    }
}
