package com.example.aicodehelper.controller.file;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件接口（基于 MinIO 对象存储）
 * 映射路径 /file，完整 URL /api/file（context-path=/api）
 * 提供上传、预览链接、下载、删除能力，供知识库/图片识别等模块复用。
 */
@Tag(name = "通用文件管理", description = "MinIO 对象存储上传/预览/下载/删除")
@RestController
@RequestMapping("/file")
public class FileController {

    /** 未指定目录前缀时的默认分类 */
    private static final String DEFAULT_PREFIX = "common";

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 通用上传：返回 objectKey 与临时预览链接
     *
     * @param file   上传文件（表单字段名 file）
     * @param prefix 目录前缀，如 knowledge/pdf、fabric/image，缺省为 common
     */
    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "prefix", required = false) String prefix) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        String dir = StringUtils.hasText(prefix) ? prefix : DEFAULT_PREFIX;
        try {
            String objectKey = fileStorageService.upload(file, dir);
            String url = fileStorageService.getPreviewUrl(objectKey);
            Map<String, Object> data = new HashMap<>();
            data.put("objectKey", objectKey);
            data.put("url", url);
            data.put("originalName", file.getOriginalFilename());
            data.put("size", file.getSize());
            return Result.success("上传成功", data);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取临时预览链接（有效期 30 分钟）
     */
    @Operation(summary = "获取预览链接")
    @GetMapping("/preview")
    public Result preview(@RequestParam("objectKey") String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return Result.error("objectKey 不能为空");
        }
        try {
            return Result.success(fileStorageService.getPreviewUrl(objectKey));
        } catch (Exception e) {
            return Result.error("获取预览链接失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件（以附件形式返回文件流）
     */
    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("objectKey") String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return ResponseEntity.badRequest().body(Result.error("objectKey 不能为空"));
        }
        try {
            InputStream stream = fileStorageService.getFileStream(objectKey);
            String fileName = objectKey.contains("/")
                    ? objectKey.substring(objectKey.lastIndexOf('/') + 1)
                    : objectKey;
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Result.error("下载失败：" + e.getMessage()));
        }
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result delete(@RequestParam("objectKey") String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return Result.error("objectKey 不能为空");
        }
        try {
            fileStorageService.deleteFile(objectKey);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
