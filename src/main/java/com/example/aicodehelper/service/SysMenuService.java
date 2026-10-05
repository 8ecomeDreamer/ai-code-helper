package com.example.aicodehelper.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.aicodehelper.domain.SysMenu;
import com.example.aicodehelper.domain.dto.MenuQuery;
import com.example.aicodehelper.domain.vo.RouterVo;
import com.example.aicodehelper.domain.vo.TreeSelect;
import java.util.List;

/**
* @author Jim
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service
*/
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单列表（超级管理员查全部，普通用户按权限过滤）
     */
    List<SysMenu> selectMenuList(MenuQuery query);

    /**
     * 查询当前用户的菜单树（目录+菜单，用于构建动态路由）
     */
    List<SysMenu> selectMenuTreeByUserId();

    /**
     * 将菜单树构建为前端动态路由结构
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

    /**
     * 构建菜单树结构
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     * 构建前端树控件结构（id/label/children）
     */
    List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus);

    /**
     * 根据角色ID查询已分配的菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 是否存在子菜单
     */
    boolean hasChildByMenuId(Long menuId);

    /**
     * 菜单是否已分配给角色
     */
    boolean checkMenuUsedByRole(Long menuId);

    /**
     * 同一父级下菜单名称是否唯一
     */
    boolean checkMenuNameUnique(SysMenu menu);
}
