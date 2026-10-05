package com.example.aicodehelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicodehelper.domain.SysMenu;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author Jim
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Mapper
* @Entity com.example.aicodehelper.domain.SysMenu
*/
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询菜单权限标识集合
     */
    @Select("select distinct m.perms from sys_menu m "
            + "left join sys_role_menu rm on m.menu_id = rm.menu_id "
            + "left join sys_user_role ur on rm.role_id = ur.role_id "
            + "where ur.user_id = #{userId} and m.status = '0' and m.perms is not null and m.perms != ''")
    List<String> selectMenuPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询全部有效菜单树（目录+菜单，超级管理员使用）
     */
    @Select("select * from sys_menu where menu_type in ('M', 'C') and status = '0' order by parent_id, order_num")
    List<SysMenu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单树（目录+菜单）
     */
    @Select("select distinct m.* from sys_menu m "
            + "left join sys_role_menu rm on m.menu_id = rm.menu_id "
            + "left join sys_user_role ur on rm.role_id = ur.role_id "
            + "where ur.user_id = #{userId} and m.menu_type in ('M', 'C') and m.status = '0' "
            + "order by m.parent_id, m.order_num")
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询菜单列表（普通用户，带名称/状态过滤）
     */
    @Select("<script>"
            + "select distinct m.* from sys_menu m "
            + "left join sys_role_menu rm on m.menu_id = rm.menu_id "
            + "left join sys_user_role ur on rm.role_id = ur.role_id "
            + "where ur.user_id = #{userId} and m.menu_type in ('M', 'C', 'F') and m.status = '0' "
            + "<if test='menu.menu_name != null and menu.menu_name != \"\"'> and m.menu_name like concat('%', #{menu.menu_name}, '%')</if> "
            + "<if test='menu.status != null and menu.status != \"\"'> and m.status = #{menu.status}</if> "
            + "order by m.parent_id, m.order_num"
            + "</script>")
    List<SysMenu> selectMenuListByUserId(@Param("menu") SysMenu menu, @Param("userId") Long userId);

    /**
     * 是否存在子菜单
     */
    @Select("select count(1) from sys_menu where parent_id = #{menuId}")
    int hasChildByMenuId(@Param("menuId") Long menuId);

    /**
     * 菜单是否已分配给角色
     */
    @Select("select count(1) from sys_role_menu where menu_id = #{menuId}")
    int countRoleMenuByMenuId(@Param("menuId") Long menuId);

    /**
     * 同一父级下菜单名称是否唯一
     */
    @Select("<script>"
            + "select count(1) from sys_menu where menu_name = #{menu.menu_name} and parent_id = #{menu.parent_id} "
            + "<if test='menu.menu_id != null'> and menu_id != #{menu.menu_id}</if>"
            + "</script>")
    int countMenuByNameAndParent(@Param("menu") SysMenu menu);

    /**
     * 根据角色ID查询已分配的菜单ID集合
     */
    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
