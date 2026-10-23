package com.example.textileMultiModalAgent.controller.ai;

import com.example.textileMultiModalAgent.ai.agent.QaAgent;
import com.example.textileMultiModalAgent.common.core.AiJson;
import com.example.textileMultiModalAgent.common.security.TokenService;
import com.example.textileMultiModalAgent.service.admin.QuotaService;
import com.example.textileMultiModalAgent.service.ai.ChatAttachmentService;
import com.example.textileMultiModalAgent.service.ai.ChatHistoryService;
import com.example.textileMultiModalAgent.service.ai.KeywordRewriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/ai")
@Tag(name = "AiQAController", description = "AI综合问答界面接口")
public class AiQAController {


    @Resource
    private QaAgent qaAgent;

    @Resource
    private ChatAttachmentService chatAttachmentService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private TokenService tokenService;

    @Resource
    private QuotaService quotaService;

    @Resource
    private KeywordRewriteService keywordRewriteService;


    /**
     * 纯文本综合问答（GET SSE，兼容浏览器 EventSource）。
     *
     * @param memoryId 记忆id, 记忆隔离
     * @param message 用户输入信息
     */
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@RequestParam("memoryId") int memoryId,
                                              @RequestParam("message") String message,
                                              HttpServletRequest request) {
        Long userId = resolveUserId(request);
        // 按用户当日 token 预算拦截（超限直接回一帧提示，不再发起模型调用）
        if (quotaService.overBudget(userId)) {
            return Flux.just(quotaFrame());
        }
        // 关键词术语映射改写只作用于发给模型的查询串，历史仍存原文
        String effective = keywordRewriteService.rewrite(message);
        return toSse(persistRound(memoryId, message,
                qaAgent.chatStream(memoryId, effective, userId), false, userId));
    }

    /**
     * agent综合问答（POST multipart）：支持文本、文档、图片，流式输出。
     *
     * @param memoryId 记忆id, 记忆隔离
     * @param message  用户输入文字（可为空，如仅发图片）
     * @param images   粘贴/上传的图片（最多 10 张，单张 ≤ 10MB）
     * @param files    上传的文档（txt/md/pdf）
     */
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Flux<ServerSentEvent<String>> chatWithAttachments(
            @RequestParam("memoryId") int memoryId,
            @RequestParam(name = "message", required = false, defaultValue = "") String message,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "files", required = false) List<MultipartFile> files,
            HttpServletRequest request) {
        Long userId = resolveUserId(request);
        // 文档/图片多模态轮消耗巨大，入口先按预算拦截，超限不落附件、不发起模型调用
        if (quotaService.overBudget(userId)) {
            return Flux.just(quotaFrame());
        }
        // 有图片/文档时：先存 MinIO（cw-ai/{会话id}/）并写消息表+附件表，失败不中断问答
        chatAttachmentService.persistUserMessage(memoryId, message, images, files);

        String effective = keywordRewriteService.rewrite(message);
        Flux<String> stream;
        boolean hasImages = images != null && !images.isEmpty();
        boolean hasFiles = files != null && !files.isEmpty();
        if (hasImages || hasFiles) {
            // 图片/文档轮：走自定义高阶多模态 API（官方 SDK 流式，PDF 自动切 qwen3.8）
            List<String> dataUris = new ArrayList<>();
            if (hasImages) {
                for (MultipartFile image : images) {
                    try {
                        dataUris.add(toDataUri(image));
                    } catch (Exception e) {
                        // 单张不可读跳过，不中断整轮问答
                    }
                }
            }
            stream = dataUris.isEmpty() && !hasFiles
                    ? qaAgent.chatStream(memoryId, effective, userId)
                    : qaAgent.multiModelChat(dataUris, files, effective, memoryId, userId);
        } else {
            // 纯文本轮：仍走 AiServices 声明式链路
            stream = qaAgent.chatStream(memoryId, effective, userId);
        }
        // user 轮已由 persistUserMessage 落库，此处只保障会话行并归档 assistant 回复
        return toSse(persistRound(memoryId, message, stream, true, userId));
    }

    /**
     * 问答轮次落库：保障会话行 + user 消息（可选）+ 流式结束后整段归档 assistant 回复。
     * 完成/中断/异常均归档已产出的内容，保证历史记录与用户所见一致。
     *
     * @param userRoundPersisted user 消息是否已落库（带附件轮由 ChatAttachmentService 写入）
     * @param userId             当前登录用户id（可为 null），用于会话归属，供历史记录按用户查询
     */
    private Flux<String> persistRound(long sessionId, String userMessage, Flux<String> stream,
                                      boolean userRoundPersisted, Long userId) {
        chatHistoryService.ensureSession(sessionId, userId, userMessage);
        if (!userRoundPersisted) {
            chatHistoryService.saveUserMessage(sessionId, userMessage);
        }
        StringBuilder full = new StringBuilder();
        AtomicBoolean saved = new AtomicBoolean(false);
        return stream.doOnNext(full::append).doFinally(signal -> {
            if (saved.compareAndSet(false, true) && full.length() > 0) {
                chatHistoryService.saveAssistantReply(sessionId, full.toString());
            }
        });
    }

    /**
     * 解析当前登录用户id。
     *
     * <p>/ai/** 免鉴权（不经过 JwtAuthInterceptor 装载 ThreadLocal），故直接从令牌解析：
     * POST 走 Authorization 头，GET SSE 走 ?token= 查询参数（EventSource 无法设头）。
     * 未登录或令牌非法返回 null。</p>
     */
    private Long resolveUserId(HttpServletRequest request) {
        try {
            return tokenService.getUserIdFromRequest(request);
        } catch (Exception e) {
            return null;
        }
    }

    /** 上传图片转 base64 data URI（DashScope 多模态 content 的 image 字段支持该格式） */
    private String toDataUri(MultipartFile file) throws Exception {
        String mime = file.getContentType() == null ? "image/jpeg" : file.getContentType();
        return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
    }

    /**
     * 统一包装 SSE 帧：以 JSON 帧 {"d":增量文本} 下发。
     * SSE 规范会将含换行的 data 拆成多行，fetch 流式解析器无法还原；
     * JSON 转义后单帧单行，前端 decodeChunk 可完整还原换行/空白结构。
     */
    private Flux<ServerSentEvent<String>> toSse(Flux<String> stream) {
        return stream.map(chunk -> {
            String frame = encodeFrame(chunk);
            return ServerSentEvent.builder(frame).data(frame).build();
        });
    }

    /** 超预算提示帧（复用增量文本协议，前端按普通回复渲染） */
    private ServerSentEvent<String> quotaFrame() {
        String tip = "⚠️ 今日 token 用量已达上限（预算 " + quotaService.dailyTokenBudget()
                + " tokens），请明天再试或联系管理员调整额度。";
        String frame = encodeFrame(tip);
        return ServerSentEvent.builder(frame).data(frame).build();
    }

    /** 增量文本转 JSON 帧；序列化异常时降级为原文 */
    private String encodeFrame(String chunk) {
        try {
            return "{\"d\":" + AiJson.mapper().writeValueAsString(chunk) + "}";
        } catch (Exception e) {
            return chunk;
        }
    }
}
