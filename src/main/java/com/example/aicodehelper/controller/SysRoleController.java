package com.example.aicodehelper.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.domain.SysRole;
import com.example.aicodehelper.service.SysRoleService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理接口：角色列表与详情查询
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    /**
     * 角色列表（仅未删除角色，按显示顺序排序）
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/list")
    public Result list() {
        List<SysRole> roles = sysRoleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDel_flag, Constants.DEL_FLAG_NORMAL)
                .orderByAsc(SysRole::getRole_sort));
        return Result.success(roles);
    }

    /**
     * 角色详情
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/{roleId}")
    public Result getInfo(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getById(roleId));
    }
}
