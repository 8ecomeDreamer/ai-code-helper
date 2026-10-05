package com.example.aicodehelper.common.core;

import com.example.aicodehelper.domain.SysUser;
import java.util.Set;
import lombok.Data;

/**
 * 登录用户身份上下文
 */
@Data
public class LoginUser {

    /** 用户ID */
    private Long userId;

    /** 用户账号 */
    private String userName;

    /** 用户详细信息 */
    private SysUser user;

    /** 角色标识集合（role_key） */
    private Set<String> roles;

    /** 菜单权限标识集合（perms） */
    private Set<String> permissions;

    /** 登录时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;
}
