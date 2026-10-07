package com.example.aicodehelper.controller;

import com.example.aicodehelper.domain.AiChatSession;
import com.example.aicodehelper.service.AiChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天会话管理", description = "管理 AI 会话记录")
@RestController
@RequestMapping("/api/chat-session")
public class AiChatSessionController {

    @Autowired
    private AiChatSessionService aiChatSessionService;

    @Operation(summary = "获取所有会话")
    @GetMapping
    public List<AiChatSession> getAllSessions() {
        return aiChatSessionService.list();
    }

    @Operation(summary = "根据用户 ID 获取会话")
    @GetMapping("/user/{userId}")
    public List<AiChatSession> getSessionsByUserId(@PathVariable Long userId) {
        return aiChatSessionService.lambdaQuery().eq(AiChatSession::getId, userId).list();
    }

    @Operation(summary = "添加新会话")
    @PostMapping
    public boolean addSession(@RequestBody AiChatSession session) {
        return aiChatSessionService.save(session);
    }
}
