package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.ai.app.AiCodeHelperService;
import com.example.aicodehelper.ai.support.AiJson;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.ai.ChatAttachmentService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {


    @Resource
    private AiCodeHelperService aiCodeHelperService;

//    @Resource
//    private IntentRouterService intentRouterService;
//
//    @Resource
//    private MultimodalChatService multimodalChatService;

    @Resource
    private ChatAttachmentService chatAttachmentService;


    /**
     * 纯文本综合问答（GET SSE，兼容浏览器 EventSource）。
     *
     * @param memoryId 记忆id, 记忆隔离
     * @param message 用户输入信息
     */
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(@RequestParam("memoryId") int memoryId,
                                              @RequestParam("message") String message) {
        return toSse(aiCodeHelperService.chatStream(memoryId, message));
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
        return toSse(aiCodeHelperService.chatStream(memoryId, message));
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
