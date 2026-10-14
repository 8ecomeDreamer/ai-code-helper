package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP 数据接口（模拟外部业务系统）。
 * 映射路径 /erp，完整 URL /api/erp（context-path=/api）。
 */
@Tag(name = "ERP 数据", description = "模拟调取外部 ERP 系统的面料成本数据")
@RestController
@RequestMapping("/erp")
public class ErpController {

//    @Resource
//    private ErpService erpService;
//
//    /**
//     * 拉取 ERP 面料成本主数据（确定性接口调用，非 Agent）。
//     */
//    @Operation(summary = "ERP 面料成本列表")
//    @GetMapping("/fabric/list")
//    public Result listFabrics() {
//        return Result.success(erpService.listFabrics());
//    }
}
