package com.example.aicodehelper.controller.ai;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.service.ai.AiFabricItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ERP 数据接口（布号主数据表 ai_fabric_item）。
 * 映射路径 /erp，完整 URL /api/erp（context-path=/api）。
 */
@Tag(name = "ERP 数据", description = "布号主数据（面料成本五项拆分），智能报价取数入口")
@RestController
@RequestMapping("/erp")
public class ErpController {

    @Resource
    private AiFabricItemService aiFabricItemService;

    /**
     * 拉取上架面料布号列表（确定性接口调用，非 Agent）。
     *
     * @return data 为面料 VO 列表（含成本五项拆分，前端 computeQuote 直接消费）
     */
    @Operation(summary = "ERP 面料成本列表")
    @GetMapping("/fabric/list")
    public Result listFabrics() {
        return Result.success(aiFabricItemService.listOnSale());
    }
}
