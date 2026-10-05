package com.example.aicodehelper.controller;

import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.domain.SysDept;
import com.example.aicodehelper.service.SysDeptService;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门管理接口（若依风格 /system/dept）
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    @Resource
    private SysDeptService sysDeptService;

    /**
     * 部门列表（平铺结构，前端可自行构树）
     */
    @RequiresPermissions("system:dept:list")
    @GetMapping("/list")
    public Result list(SysDept query) {
        List<SysDept> depts = sysDeptService.selectDeptList(query);
        return Result.success(depts);
    }

    /**
     * 部门树（新增/修改部门时选择上级部门）
     */
    @RequiresPermissions("system:dept:list")
    @GetMapping("/treeselect")
    public Result treeselect() {
        List<SysDept> depts = sysDeptService.selectDeptList(null);
        return Result.success(sysDeptService.buildDeptTreeSelect(depts));
    }

    /**
     * 部门详情
     */
    @RequiresPermissions("system:dept:query")
    @GetMapping("/{deptId}")
    public Result getInfo(@PathVariable Long deptId) {
        return Result.success(sysDeptService.getById(deptId));
    }

    /**
     * 新增部门
     */
    @RequiresPermissions("system:dept:add")
    @PostMapping
    public Result add(@RequestBody SysDept dept) {
        if (dept.getParent_id() == null) {
            dept.setParent_id(0L);
        }
        if (!sysDeptService.checkDeptNameUnique(dept)) {
            return Result.error("新增部门'" + dept.getDept_name() + "'失败，部门名称已存在");
        }
        SysDept parent = sysDeptService.checkParentDept(dept.getParent_id());
        dept.setAncestors(sysDeptService.buildAncestors(parent));
        dept.setCreate_by(SecurityUtils.getUsername());
        dept.setCreate_time(new Date());
        return sysDeptService.save(dept) ? Result.success() : Result.error();
    }

    /**
     * 修改部门（父部门变更时同步修正子孙部门祖级列表）
     */
    @RequiresPermissions("system:dept:edit")
    @PutMapping
    public Result edit(@RequestBody SysDept dept) {
        if (dept.getDept_id() == null) {
            return Result.error("修改部门失败，部门ID不能为空");
        }
        if (dept.getDept_id().equals(dept.getParent_id())) {
            return Result.error("修改部门'" + dept.getDept_name() + "'失败，上级部门不能选择自己");
        }
        if (!sysDeptService.checkDeptNameUnique(dept)) {
            return Result.error("修改部门'" + dept.getDept_name() + "'失败，部门名称已存在");
        }
        SysDept old = sysDeptService.getById(dept.getDept_id());
        if (old == null) {
            return Result.error("部门不存在，无法修改");
        }
        SysDept parent = sysDeptService.checkParentDept(dept.getParent_id());
        String newAncestors = sysDeptService.buildAncestors(parent);
        // 不允许移动到本部门子孙节点下
        if (parent != null && parent.getAncestors() != null
                && ("," + parent.getAncestors() + ",").contains("," + dept.getDept_id() + ",")) {
            return Result.error("修改部门'" + dept.getDept_name() + "'失败，上级部门不能是自己的子孙部门");
        }
        dept.setAncestors(newAncestors);
        dept.setUpdate_by(SecurityUtils.getUsername());
        dept.setUpdate_time(new Date());
        boolean result = sysDeptService.updateById(dept);
        if (result && old.getAncestors() != null && !old.getAncestors().equals(newAncestors)) {
            sysDeptService.updateDeptChildren(dept.getDept_id(), old.getAncestors(), newAncestors);
        }
        return result ? Result.success() : Result.error();
    }

    /**
     * 删除部门（存在子部门或部门下存在用户时不允许删除）
     */
    @RequiresPermissions("system:dept:remove")
    @DeleteMapping("/{deptId}")
    public Result remove(@PathVariable Long deptId) {
        if (sysDeptService.hasChildByDeptId(deptId)) {
            return Result.error("存在下级部门,不允许删除");
        }
        if (sysDeptService.checkDeptExistUser(deptId)) {
            return Result.error("部门存在用户,不允许删除");
        }
        SysDept dept = new SysDept();
        dept.setDept_id(deptId);
        dept.setDel_flag("2");
        dept.setUpdate_by(SecurityUtils.getUsername());
        dept.setUpdate_time(new Date());
        return sysDeptService.updateById(dept) ? Result.success() : Result.error();
    }
}
