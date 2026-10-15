package com.example.aicodehelper.service.ai;

import com.example.aicodehelper.domain.AiChatMessage;
import com.example.aicodehelper.domain.AiChatMessageAttachment;
import com.example.aicodehelper.domain.AiChatSession;
import com.example.aicodehelper.domain.vo.ChatMessageVo;
import com.example.aicodehelper.service.file.FileStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天历史服务：会话行保障、user/assistant 消息落库、带附件的历史查询。
 *
 * <p>与 {@link ChatAttachmentService} 分工：带附件的 user 轮由后者写入（含 MinIO 上传），
 * 本服务负责会话行、纯文本 user 轮、assistant 回复归档与历史组装。</p>
 */
@Service
@Slf4j
public class ChatHistoryService {

    /** 会话标题缺省值，命中视为标题尚未生成 */
    private static final String DEFAULT_TITLE = "新对话";

    /** 会话标题最大长度 */
    private static final int TITLE_MAX = 30;

    @Resource
    private AiChatSessionService aiChatSessionService;

    @Resource
    private AiChatMessageService aiChatMessageService;

    @Resource
    private AiChatMessageAttachmentService aiChatMessageAttachmentService;

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 保障会话行存在：不存在则按指定 id 建行（id=memoryId，与对话记忆键保持一致）；
     * 已存在但标题仍为缺省值时，用首条提问更新标题。落库失败仅记日志，不影响问答。
     *
     * @param sessionId    会话id（前端 memoryId）
     * @param userId       当前登录用户id，未登录传 null
     * @param firstMessage 本轮用户提问（用于生成标题）
     */
    public void ensureSession(long sessionId, Long userId, String firstMessage) {
        try {
            Date now = new Date();
            AiChatSession session = aiChatSessionService.getById(sessionId);
            if (session == null) {
                session = new AiChatSession();
                session.setId(sessionId);
                session.setUser_id(userId);
                session.setSession_title(titleOf(firstMessage));
                session.setStatus(1);
                session.setCreate_time(now);
                session.setUpdate_time(now);
                aiChatSessionService.save(session);
                return;
            }
            String title = session.getSession_title();
            if (title == null || title.isBlank() || DEFAULT_TITLE.equals(title)) {
                session.setSession_title(titleOf(firstMessage));
                session.setUpdate_time(now);
                aiChatSessionService.updateById(session);
            }
        } catch (Exception e) {
            log.error("保障聊天会话行失败，sessionId={}", sessionId, e);
        }
    }

    /**
     * 落库一条纯文本 user 消息（GET 文本链路使用；带附件轮由 ChatAttachmentService 写入）。
     */
    public void saveUserMessage(long sessionId, String content) {
        try {
            AiChatMessage msg = new AiChatMessage();
            msg.setSession_id(sessionId);
            msg.setRole("user");
            msg.setContent(content == null ? "" : content);
            msg.setCreate_time(new Date());
            aiChatMessageService.save(msg);
        } catch (Exception e) {
            log.error("写入 user 消息失败，sessionId={}", sessionId, e);
        }
    }

    /**
     * 归档一条 assistant 回复（流式结束后整段写入）。
     */
    public void saveAssistantReply(long sessionId, String content) {
        try {
            AiChatMessage msg = new AiChatMessage();
            msg.setSession_id(sessionId);
            msg.setRole("assistant");
            msg.setContent(content == null ? "" : content);
            msg.setCreate_time(new Date());
            aiChatMessageService.save(msg);
        } catch (Exception e) {
            log.error("写入 assistant 回复失败，sessionId={}", sessionId, e);
        }
    }

    /**
     * 查询会话历史消息（时间正序），附件随消息返回并附 MinIO 临时预览链接。
     */
    public List<ChatMessageVo> history(long sessionId) {
        List<AiChatMessage> messages = aiChatMessageService.lambdaQuery()
                .eq(AiChatMessage::getSession_id, sessionId)
                .orderByAsc(AiChatMessage::getId)
                .list();
        if (messages.isEmpty()) {
            return List.of();
        }

        // 附件按消息id分组
        List<Long> messageIds = messages.stream().map(AiChatMessage::getId).toList();
        Map<Long, List<ChatMessageVo.Attachment>> attachMap = new HashMap<>();
        List<AiChatMessageAttachment> rows = aiChatMessageAttachmentService.lambdaQuery()
                .in(AiChatMessageAttachment::getMessage_id, messageIds)
                .orderByAsc(AiChatMessageAttachment::getSort_no)
                .list();
        for (AiChatMessageAttachment row : rows) {
            attachMap.computeIfAbsent(row.getMessage_id(), key -> new ArrayList<>())
                    .add(toAttachment(row));
        }

        List<ChatMessageVo> result = new ArrayList<>(messages.size());
        for (AiChatMessage msg : messages) {
            ChatMessageVo vo = new ChatMessageVo();
            vo.setId(msg.getId());
            vo.setSessionId(msg.getSession_id());
            vo.setRole(msg.getRole());
            vo.setContent(msg.getContent());
            vo.setCreateTime(msg.getCreate_time());
            vo.setAttachments(attachMap.getOrDefault(msg.getId(), List.of()));
            result.add(vo);
        }
        return result;
    }

    /** 附件行转 VO；预览链接生成失败仅置空，不影响历史展示 */
    private ChatMessageVo.Attachment toAttachment(AiChatMessageAttachment row) {
        ChatMessageVo.Attachment attach = new ChatMessageVo.Attachment();
        attach.setType(row.getAttach_type());
        attach.setFileName(row.getFile_name());
        attach.setObjectName(row.getObject_name());
        attach.setMimeType(row.getMime_type());
        attach.setFileSize(row.getFile_size());
        try {
            attach.setUrl(fileStorageService.getPreviewUrl(row.getObject_name()));
        } catch (Exception e) {
            log.warn("生成附件预览链接失败，objectName={}", row.getObject_name(), e);
        }
        return attach;
    }

    /** 会话标题：取首条提问截断；空提问（仅图片/文档）记为对应类型提问 */
    private String titleOf(String firstMessage) {
        if (firstMessage == null || firstMessage.isBlank()) {
            return "图片/文档提问";
        }
        String trimmed = firstMessage.trim();
        return trimmed.length() <= TITLE_MAX ? trimmed : trimmed.substring(0, TITLE_MAX);
    }
}
