package com.example.aicodehelper.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.domain.SysMenu;
import com.example.aicodehelper.domain.dto.MenuQuery;
import com.example.aicodehelper.domain.vo.MetaVo;
import com.example.aicodehelper.domain.vo.RouterVo;
import com.example.aicodehelper.domain.vo.TreeSelect;
import com.example.aicodehelper.mapper.SysMenuMapper;
import com.example.aicodehelper.service.admin.SysMenuService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
* @author Jim
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService {

    @Override
    public List<SysMenu> selectMenuList(MenuQuery query) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .like(StringUtils.hasText(query.getMenu_name()), SysMenu::getMenu_name, query.getMenu_name())
                .eq(StringUtils.hasText(query.getStatus()), SysMenu::getStatus, query.getStatus())
                .orderByAsc(SysMenu::getParent_id)
                .orderByAsc(SysMenu::getOrder_num);
        if (SecurityUtils.isAdmin()) {
            return baseMapper.selectList(wrapper);
        }
        // 普通用户：按 用户-角色-菜单 关联过滤
        SysMenu condition = new SysMenu();
        condition.setMenu_name(query.getMenu_name());
        condition.setStatus(query.getStatus());
        return baseMapper.selectMenuListByUserId(condition, SecurityUtils.getUserId());
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId() {
        if (SecurityUtils.isAdmin()) {
            return baseMapper.selectMenuTreeAll();
        }
        return baseMapper.selectMenuTreeByUserId(SecurityUtils.getUserId());
    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new ArrayList<>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(Constants.MENU_HIDDEN.equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            if (isDirWithChildren(menu)) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(menu.getChildren()));
            } else if (isMenuFrame(menu)) {
                // 顶级菜单（无子菜单的C类型）：外层包一级空壳路由
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(StringUtils.hasText(menu.getRoute_name())
                        ? menu.getRoute_name() : capitalize(menu.getPath()));
                children.setMeta(buildMeta(menu));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (isParentView(menu)) {
                // 目录下的子目录：ParentView 逐层包裹
                List<RouterVo> childrenList = new ArrayList<>();
                for (SysMenu child : menu.getChildren()) {
                    RouterVo childRouter = new RouterVo();
                    childRouter.setPath(child.getPath());
                    childRouter.setComponent(child.getComponent());
                    childRouter.setName(StringUtils.hasText(child.getRoute_name())
                            ? child.getRoute_name() : capitalize(child.getPath()));
                    childRouter.setMeta(buildMeta(child));
                    childRouter.setQuery(child.getQuery());
                    childrenList.add(childRouter);
                }
                router.setChildren(childrenList);
            } else {
                router.setMeta(buildMeta(menu));
            }
            routers.add(router);
        }
        return routers;
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Set<Long> ids = menus.stream().map(SysMenu::getMenu_id).collect(Collectors.toSet());
        // 根节点：父ID不在结果集中的节点
        List<SysMenu> roots = menus.stream()
                .filter(m -> m.getParent_id() == null || !ids.contains(m.getParent_id()))
                .collect(Collectors.toList());
        roots.forEach(root -> root.setChildren(getChildMenus(root, menus)));
        return roots;
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        return buildMenuTree(menus).stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return baseMapper.hasChildByMenuId(menuId) > 0;
    }

    @Override
    public boolean checkMenuUsedByRole(Long menuId) {
        return baseMapper.countRoleMenuByMenuId(menuId) > 0;
    }

    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        return baseMapper.countMenuByNameAndParent(menu) == 0;
    }

    /** 递归装配子菜单 */
    private List<SysMenu> getChildMenus(SysMenu parent, List<SysMenu> menus) {
        List<SysMenu> children = menus.stream()
                .filter(m -> parent.getMenu_id().equals(m.getParent_id()))
                .collect(Collectors.toList());
        children.forEach(child -> child.setChildren(getChildMenus(child, menus)));
        return children;
    }

    private MetaVo buildMeta(SysMenu menu) {
        MetaVo meta = new MetaVo(menu.getMenu_name(), menu.getIcon());
        meta.setNoCache(menu.getIs_cache() != null && menu.getIs_cache() == 1);
        if (isInnerLink(menu)) {
            meta.setLink(menu.getPath());
        }
        return meta;
    }

    /** 路由名称：route_name 优先，否则取 path 首字母大写 */
    private String getRouteName(SysMenu menu) {
        if (StringUtils.hasText(menu.getRoute_name())) {
            return menu.getRoute_name();
        }
        return capitalize(menu.getPath());
    }

    /** 路由地址：顶级目录/菜单框架补 "/" 前缀；内链返回 http 地址 */
    private String getRouterPath(SysMenu menu) {
        if (isTopLevel(menu) && isDir(menu)) {
            return "/" + trimSlash(menu.getPath());
        }
        if (isTopLevel(menu) && isMenuFrame(menu)) {
            return "/" + trimSlash(menu.getPath());
        }
        if (isInnerLink(menu)) {
            return menu.getPath();
        }
        return menu.getPath();
    }

    /** 组件地址：顶级目录用 Layout；内链用 InnerLink；子目录用 ParentView；否则取 component */
    private String getComponent(SysMenu menu) {
        if (isTopLevel(menu) && isDir(menu)) {
            return Constants.LAYOUT;
        }
        if (isInnerLink(menu)) {
            return Constants.INNER_LINK;
        }
        if (isParentView(menu)) {
            return Constants.PARENT_VIEW;
        }
        return StringUtils.hasText(menu.getComponent()) ? menu.getComponent() : Constants.LAYOUT;
    }

    private boolean isDir(SysMenu menu) {
        return Constants.TYPE_DIR.equals(menu.getMenu_type());
    }

    private boolean isDirWithChildren(SysMenu menu) {
        return isDir(menu) && menu.getChildren() != null && !menu.getChildren().isEmpty();
    }

    /** 顶级菜单框架：一级菜单、C类型、无子菜单、非外链 */
    private boolean isMenuFrame(SysMenu menu) {
        return isTopLevel(menu)
                && Constants.TYPE_MENU.equals(menu.getMenu_type())
                && (menu.getChildren() == null || menu.getChildren().isEmpty());
    }

    /** 非顶级目录：ParentView 逐层渲染 */
    private boolean isParentView(SysMenu menu) {
        return isDir(menu) && !isTopLevel(menu);
    }

    /** 内链：非顶级且 is_frame=0 且 path 为 http(s) 地址 */
    private boolean isInnerLink(SysMenu menu) {
        return menu.getIs_frame() != null
                && menu.getIs_frame() == Constants.YES_FRAME
                && StringUtils.hasText(menu.getPath())
                && (menu.getPath().startsWith("http://") || menu.getPath().startsWith("https://"));
    }

    private boolean isTopLevel(SysMenu menu) {
        return menu.getParent_id() == null || menu.getParent_id() == 0L;
    }

    private String trimSlash(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String capitalize(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        String trimmed = trimSlash(str);
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }
}
