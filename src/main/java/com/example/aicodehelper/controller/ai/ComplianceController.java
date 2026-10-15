package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.ai.ComplianceService;
import com.example.aicodehelper.service.ai.DocumentParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 合规校验接口。
 * 映射路径 /compliance，完整 URL /api/compliance（context-path=/api）。
 */
@Tag(name = "合规校验", description = "解析 txt/md/pdf 文档并由大模型深度审查出报告")
@RestController
@RequestMapping("/compliance")
public class ComplianceController {

    @Resource
    private ComplianceService complianceService;

    @Resource
    private DocumentParseService documentParseService;

    /**
     * 合规校验：上传文档（txt/md/pdf）或直接粘贴条款文本，二选一即可。
     *
     * @param file 条款文档（可选）
     * @param text 条款文本（可选，未上传文件时使用）
     * @return data 为 {items, risks, summary, source} 校验报告
     */
    @Operation(summary = "合同条款合规校验")
    @PostMapping("/check")
    public Result check(@RequestParam(value = "file", required = false) MultipartFile file,
                        @RequestParam(value = "text", required = false) String text) {
        try {
            String content;
            if (file != null && !file.isEmpty()) {
                content = documentParseService.parse(file);
            } else if (StringUtils.hasText(text)) {
                content = text;
            } else {
                return Result.error("请上传条款文档或粘贴条款文本");
            }
            if (!StringUtils.hasText(content)) {
                return Result.error("文档解析结果为空，请确认文件内容或改用粘贴方式");
            }
            return Result.success("合规校验完成", complianceService.check(content));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("合规校验失败：" + e.getMessage());
        }
    }
}
