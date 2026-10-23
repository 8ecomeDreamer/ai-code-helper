package com.example.textileMultiModalAgent.controller.ai;

import com.example.textileMultiModalAgent.common.utils.SecurityUtils;
import com.example.textileMultiModalAgent.domain.AiChatSession;
import com.example.textileMultiModalAgent.service.ai.AiChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "聊天会话管理", description = "管理 AI 会话记录")
@RestController
@RequestMapping("/chat-session")
public class AiChatSessionController {

    @Autowired
    private AiChatSessionService aiChatSessionService;

    @Operation(summary = "获取所有会话")
    @GetMapping
    public List<AiChatSession> getAllSessions() {
        return aiChatSessionService.lambdaQuery()
                .orderByDesc(AiChatSession::getId)
                .list();
    }

    @Operation(summary = "获取当前登录用户的会话（聊天页侧边栏历史）")
    @GetMapping("/my")
    public List<AiChatSession> getMySessions() {
        Long userId = SecurityUtils.getUserId();
        return aiChatSessionService.lambdaQuery()
                .eq(AiChatSession::getUser_id, userId)
                .orderByDesc(AiChatSession::getId)
                .list();
    }

    @Operation(summary = "根据用户 ID 获取会话")
    @GetMapping("/user/{userId}")
    public List<AiChatSession> getSessionsByUserId(@PathVariable Long userId) {
        return aiChatSessionService.lambdaQuery()
                .eq(AiChatSession::getUser_id, userId)
                .orderByDesc(AiChatSession::getId)
                .list();
    }

    @Operation(summary = "根据 ID 获取会话")
    @GetMapping("/{id}")
    public AiChatSession getSessionById(@PathVariable Long id) {
        return aiChatSessionService.getById(id);
    }

    @Operation(summary = "添加新会话")
    @PostMapping
    public AiChatSession addSession(@RequestBody AiChatSession session) {
        aiChatSessionService.save(session);
        return session;
    }

    @Operation(summary = "修改会话")
    @PutMapping
    public boolean updateSession(@RequestBody AiChatSession session) {
        return aiChatSessionService.updateById(session);
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{id}")
    public boolean deleteSession(@PathVariable Long id) {
        return aiChatSessionService.removeById(id);
    }
}
