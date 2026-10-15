package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.ai.app.service.AiCodeHelperHigherService;
import com.example.aicodehelper.ai.app.service.AiCodeHelperService;
import com.example.aicodehelper.common.core.AiJson;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.service.ai.ChatAttachmentService;
import com.example.aicodehelper.service.ai.ChatHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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
    private AiCodeHelperService aiCodeHelperService;

    @Resource
    private AiCodeHelperHigherService aiCodeHelperHigherService;

    @Resource
    private ChatAttachmentService chatAttachmentService;

    @Resource
    private ChatHistoryService chatHistoryService;


    /**
     * 纯文本综合问答（GET SSE，兼容浏览器 EventSource）。
     *
     * @param memoryId 记忆id, 记忆隔离
     * @param message 用户输入信息
     */
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@RequestParam("memoryId") int memoryId,
                                              @RequestParam("message") String message) {
        return toSse(persistRound(memoryId, message,
                aiCodeHelperService.chatStream(memoryId, message), false));
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
            @RequestParam(name = "files", required = false) List<MultipartFile> files) {
        // 有图片/文档时：先存 MinIO（cw-ai/{会话id}/）并写消息表+附件表，失败不中断问答
        chatAttachmentService.persistUserMessage(memoryId, message, images, files);

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
                    ? aiCodeHelperService.chatStream(memoryId, message)
                    : aiCodeHelperHigherService.multiModelChat(dataUris, files, message);
        } else {
            // 纯文本轮：仍走 AiServices 声明式链路
            stream = aiCodeHelperService.chatStream(memoryId, message);
        }
        // user 轮已由 persistUserMessage 落库，此处只保障会话行并归档 assistant 回复
        return toSse(persistRound(memoryId, message, stream, true));
    }

    /**
     * 问答轮次落库：保障会话行 + user 消息（可选）+ 流式结束后整段归档 assistant 回复。
     * 完成/中断/异常均归档已产出的内容，保证历史记录与用户所见一致。
     *
     * @param userRoundPersisted user 消息是否已落库（带附件轮由 ChatAttachmentService 写入）
     */
    private Flux<String> persistRound(long sessionId, String userMessage, Flux<String> stream,
                                      boolean userRoundPersisted) {
        chatHistoryService.ensureSession(sessionId, currentUserId(), userMessage);
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

    /** /ai/** 免鉴权，未登录时返回 null（会话 user_id 置空） */
    private Long currentUserId() {
        try {
            return SecurityUtils.getUserId();
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

    /** 增量文本转 JSON 帧；序列化异常时降级为原文 */
    private String encodeFrame(String chunk) {
        try {
            return "{\"d\":" + AiJson.mapper().writeValueAsString(chunk) + "}";
        } catch (Exception e) {
            return chunk;
        }
    }

//    /**
//     * 总路由：识别用户请求意图，返回应转发到的专属子 Agent / 接口。
//     * 与 /ai/chat 同为公开入口（/ai/** 免鉴权），供综合问答页做「先分类再分发」。
//     *
//     * @param message 用户自然语言请求
//     * @return data 为 {intent, label, endpoint, suggestion}
//     */
//    @GetMapping("/route")
//    public Result route(@RequestParam("message") String message) {
//        if (!StringUtils.hasText(message)) {
//            return Result.error("message 不能为空");
//        }
//        try {
//            return Result.success(intentRouterService.route(message));
//        } catch (Exception e) {
//            return Result.error("意图识别失败：" + e.getMessage());
//        }
//    }
}
