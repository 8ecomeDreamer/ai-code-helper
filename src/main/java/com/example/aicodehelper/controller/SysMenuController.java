package com.example.aicodehelper.controller;

import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.domain.SysMenu;
import com.example.aicodehelper.domain.dto.MenuQuery;
import com.example.aicodehelper.service.SysMenuService;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单管理接口（若依风格 /system/menu）
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService sysMenuService;

    /**
     * 菜单列表（超级管理员查全部，普通用户按权限过滤）
     */
    @RequiresPermissions("system:menu:list")
    @GetMapping("/list")
    public Result list(MenuQuery query) {
        List<SysMenu> menus = sysMenuService.selectMenuList(query);
        return Result.success(menus);
    }

    /**
     * 菜单树（新增/修改菜单时选择上级菜单）
     */
    @RequiresPermissions("system:menu:query")
    @GetMapping("/treeselect")
    public Result treeselect() {
        List<SysMenu> menus = sysMenuService.selectMenuList(new MenuQuery());
        return Result.success(sysMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 角色菜单树：menus 为全量树，checkedKeys 为该角色已勾选菜单ID
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public Result roleMenuTreeselect(@PathVariable Long roleId) {
        List<SysMenu> menus = sysMenuService.selectMenuList(new MenuQuery());
        return Result.success()
                .put("menus", sysMenuService.buildMenuTreeSelect(menus))
                .put("checkedKeys", sysMenuService.selectMenuIdsByRoleId(roleId));
    }

    /**
     * 菜单详情
     */
    @RequiresPermissions("system:menu:query")
    @GetMapping("/{menuId}")
    public Result getInfo(@PathVariable Long menuId) {
        return Result.success(sysMenuService.getById(menuId));
    }

    /**
     * 新增菜单
     */
    @RequiresPermissions("system:menu:add")
    @PostMapping
    public Result add(@Validated @RequestBody SysMenu menu) {
        if (menu.getParent_id() == null) {
            menu.setParent_id(0L);
        }
        if (!sysMenuService.checkMenuNameUnique(menu)) {
            return Result.error("新增菜单'" + menu.getMenu_name() + "'失败，菜单名称已存在");
        }
        menu.setCreate_time(new Date());
        menu.setUpdate_time(new Date());
        return sysMenuService.save(menu) ? Result.success() : Result.error();
    }

    /**
     * 修改菜单
     */
    @RequiresPermissions("system:menu:edit")
    @PutMapping
    public Result edit(@Validated @RequestBody SysMenu menu) {
        if (menu.getMenu_id() == null) {
            return Result.error("修改菜单失败，菜单ID不能为空");
        }
        if (!sysMenuService.checkMenuNameUnique(menu)) {
            return Result.error("修改菜单'" + menu.getMenu_name() + "'失败，菜单名称已存在");
        }
        if (menu.getMenu_id().equals(menu.getParent_id())) {
            return Result.error("修改菜单'" + menu.getMenu_name() + "'失败，上级菜单不能选择自己");
        }
        menu.setUpdate_time(new Date());
        return sysMenuService.updateById(menu) ? Result.success() : Result.error();
    }

    /**
     * 删除菜单（存在子菜单或已分配角色时不允许删除）
     */
    @RequiresPermissions("system:menu:remove")
    @DeleteMapping("/{menuId}")
    public Result remove(@PathVariable Long menuId) {
        if (sysMenuService.hasChildByMenuId(menuId)) {
            return Result.error("存在子菜单,不允许删除");
        }
        if (sysMenuService.checkMenuUsedByRole(menuId)) {
            return Result.error("菜单已分配,不允许删除");
        }
        return sysMenuService.removeById(menuId) ? Result.success() : Result.error();
    }
}
