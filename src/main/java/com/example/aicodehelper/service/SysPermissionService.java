package com.example.aicodehelper.service;

import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.domain.SysRole;
import com.example.aicodehelper.domain.SysUser;
import com.example.aicodehelper.mapper.SysMenuMapper;
import com.example.aicodehelper.mapper.SysRoleMapper;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 用户权限处理：角色权限 + 菜单权限
 */
@Service
public class SysPermissionService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysMenuMapper sysMenuMapper;

    /**
     * 获取用户的角色标识集合（role_key）
     */
    public Set<String> getRolePermission(SysUser user) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getUser_id());
        return roles.stream()
                .map(SysRole::getRole_key)
                .filter(key -> key != null && !key.isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的菜单权限标识集合（perms），超级管理员拥有全部权限
     */
    public Set<String> getMenuPermission(SysUser user, Set<String> roles) {
        Set<String> permissions = new HashSet<>();
        if (roles.contains(Constants.ADMIN_ROLE_KEY)) {
            permissions.add(Constants.ALL_PERMISSION);
        } else {
            List<String> perms = sysMenuMapper.selectMenuPermsByUserId(user.getUser_id());
            perms.stream()
                    .filter(p -> p != null && !p.isBlank())
                    .forEach(permissions::add);
        }
        return permissions;
    }
}
