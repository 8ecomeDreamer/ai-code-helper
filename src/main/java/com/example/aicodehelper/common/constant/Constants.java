package com.example.aicodehelper.common.constant;

/**
 * 通用常量
 */
public class Constants {

    /** 成功标记 */
    public static final int SUCCESS = 200;

    /** 失败标记 */
    public static final int FAIL = 500;

    /** 未认证 */
    public static final int UNAUTHORIZED = 401;

    /** 无权限 */
    public static final int FORBIDDEN = 403;

    /** 超级管理员角色标识 */
    public static final String ADMIN_ROLE_KEY = "admin";

    /** 全部权限标识 */
    public static final String ALL_PERMISSION = "*:*:*";

    /** 菜单类型：目录 */
    public static final String TYPE_DIR = "M";

    /** 菜单类型：菜单 */
    public static final String TYPE_MENU = "C";

    /** 菜单类型：按钮 */
    public static final String TYPE_BUTTON = "F";

    /** 菜单显示状态：显示 */
    public static final String MENU_VISIBLE = "0";

    /** 菜单显示状态：隐藏 */
    public static final String MENU_HIDDEN = "1";

    /** 是否外链：是 */
    public static final int YES_FRAME = 0;

    /** 是否外链：否 */
    public static final int NO_FRAME = 1;

    /** 正常状态 */
    public static final String STATUS_NORMAL = "0";

    /** 未删除标志 */
    public static final String DEL_FLAG_NORMAL = "0";

    /** 顶级路由组件：布局 */
    public static final String LAYOUT = "Layout";

    /** 父级视图组件 */
    public static final String PARENT_VIEW = "ParentView";

    /** 内链组件 */
    public static final String INNER_LINK = "InnerLink";

    private Constants() {
    }
}
