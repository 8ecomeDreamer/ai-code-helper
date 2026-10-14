package com.example.aicodehelper.service.ai;

import com.example.aicodehelper.ai.support.AiJson;
import com.example.aicodehelper.domain.AiChatMessage;
import com.example.aicodehelper.domain.AiChatMessageAttachment;
import com.example.aicodehelper.service.ai.AiChatMessageAttachmentService;
import com.example.aicodehelper.service.ai.AiChatMessageService;
import com.example.aicodehelper.service.file.FileStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天附件落库服务：带图片/文档发送问题时，先把附件上传到 MinIO
 * （bucket 取配置 minio.bucket-name=cw-ai，文件夹名=会话id），
 * 再写入 ai_chat_message（本轮 user 消息）与 ai_chat_message_attachment（附件对象存储记录）。
 *
 * <p>单个附件上传失败仅跳过该附件并记日志，不中断问答主流程。</p>
 */
@Service
@Slf4j
public class ChatAttachmentService {

    /** 附件类型：图片 */
    public static final String TYPE_IMAGE = "image";
    /** 附件类型：文档附件 */
    public static final String TYPE_FILE = "file";

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private AiChatMessageService aiChatMessageService;

    @Resource
    private AiChatMessageAttachmentService aiChatMessageAttachmentService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 持久化本轮 user 消息及其附件。
     *
     * @param sessionId 会话id（memoryId），同时作为 MinIO 文件夹名
     * @param message   用户输入文字（可为空）
     * @param images    图片附件，可为 null
     * @param files     文档附件，可为 null
     * @return 消息id；无附件时返回 null（不落库）
     */
    public Long persistUserMessage(long sessionId, String message,
                                   List<MultipartFile> images, List<MultipartFile> files) {
        List<MultipartFile> imgs = images == null ? List.of() : images;
        List<MultipartFile> docs = files == null ? List.of() : files;
        if (imgs.isEmpty() && docs.isEmpty()) {
            return null;
        }

        // 1. 逐个上传 MinIO（文件夹=会话id），收集元数据与附件行
        List<Map<String, Object>> modalMeta = new ArrayList<>();
        List<AiChatMessageAttachment> rows = new ArrayList<>();
        int sort = 0;
        for (MultipartFile image : imgs) {
            sort = uploadAndCollect(image, sessionId, TYPE_IMAGE, sort, modalMeta, rows);
        }
        for (MultipartFile doc : docs) {
            sort = uploadAndCollect(doc, sessionId, TYPE_FILE, sort, modalMeta, rows);
        }

        // 2. 写本轮 user 消息，多模态元数据JSON存入 multi_modal_data
        AiChatMessage msg = new AiChatMessage();
        msg.setSession_id(sessionId);
        msg.setRole("user");
        msg.setContent(message == null ? "" : message);
        msg.setCreate_time(new Date());
        if (!modalMeta.isEmpty()) {
            try {
                msg.setMulti_modal_data(AiJson.mapper().writeValueAsString(modalMeta));
            } catch (Exception e) {
                log.warn("多模态元数据序列化失败，sessionId={}", sessionId, e);
            }
        }
        if (!aiChatMessageService.save(msg)) {
            log.error("写入 ai_chat_message 失败，sessionId={}", sessionId);
            return null;
        }

        // 3. 写附件记录（回填消息id）
        if (!rows.isEmpty()) {
            for (AiChatMessageAttachment row : rows) {
                row.setMessage_id(msg.getId());
            }
            aiChatMessageAttachmentService.saveBatch(rows);
        }
        return msg.getId();
    }

    /** 上传单个附件到 MinIO 并生成元数据与附件行；失败跳过该附件，返回原 sort */
    private int uploadAndCollect(MultipartFile file, long sessionId, String type, int sort,
                                 List<Map<String, Object>> modalMeta, List<AiChatMessageAttachment> rows) {
        String originalName = file.getOriginalFilename();
        try {
            String objectKey = fileStorageService.upload(file, String.valueOf(sessionId));

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("type", type);
            meta.put("fileName", originalName);
            meta.put("objectName", objectKey);
            meta.put("mimeType", file.getContentType());
            meta.put("size", file.getSize());
            modalMeta.add(meta);

            AiChatMessageAttachment row = new AiChatMessageAttachment();
            row.setSession_id(sessionId);
            row.setAttach_type(type);
            row.setFile_name(originalName == null || originalName.isBlank() ? "未命名" : originalName);
            row.setFile_ext(extOf(originalName));
            row.setMime_type(file.getContentType());
            row.setFile_size(file.getSize());
            row.setBucket_name(bucketName);
            row.setObject_name(objectKey);
            row.setSort_no(sort);
            row.setCreate_time(new Date());
            rows.add(row);
            return sort + 1;
        } catch (Exception e) {
            log.error("聊天附件上传 MinIO 失败，sessionId={}，fileName={}", sessionId, originalName, e);
            return sort;
        }
    }

    /** 文件扩展名（不含点、小写）；无扩展名返回 null */
    private String extOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
