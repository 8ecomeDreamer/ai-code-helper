package com.example.aicodehelper.common.aspect;

import com.example.aicodehelper.common.annotation.RequiresPermissions;
import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.exception.NotPermissionException;
import com.example.aicodehelper.common.utils.SecurityUtils;
import java.util.Set;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面：拦截 @RequiresPermissions，校验登录用户的菜单权限标识
 */
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requiresPermissions)")
    public void checkPermission(RequiresPermissions requiresPermissions) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Set<String> permissions = loginUser.getPermissions();
        for (String permission : requiresPermissions.value()) {
            if (!hasPermission(permissions, permission)) {
                throw new NotPermissionException(permission);
            }
        }
    }

    private boolean hasPermission(Set<String> permissions, String permission) {
        return permissions != null
                && (permissions.contains(Constants.ALL_PERMISSION) || permissions.contains(permission));
    }
}
