package com.example.textileMultiModalAgent.controller.ai;

import com.example.textileMultiModalAgent.common.core.Result;
import com.example.textileMultiModalAgent.service.ai.FabricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 面料识别接口。
 * 映射路径 /fabric，完整 URL /api/fabric（context-path=/api）。
 */
@Tag(name = "面料识别", description = "视觉模型检测面料疵点，非面料图片拒绝")
@RestController
@RequestMapping("/fabric")
public class FabricController {

    /** 单张图片上限 10MB，与前端约束一致 */
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    @Resource
    private FabricService fabricService;

    /**
     * 面料疵点检测（multipart 图片）。
     *
     * @param file 面料图片
     * @return data 为 {status, title, message, report?}；非面料图片 status=error
     */
    @Operation(summary = "面料疵点检测")
    @PostMapping("/defect/detect")
    public Result detect(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传面料图片");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            return Result.error("仅支持图片文件（JPG / PNG 等）");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error("图片超过 10MB，请压缩后重试");
        }
        try {
            return Result.success(fabricService.detect(file.getBytes(), contentType));
        } catch (Exception e) {
            return Result.error("疵点检测失败：" + e.getMessage());
        }
    }
}
