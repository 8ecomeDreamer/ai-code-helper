package com.example.aicodehelper.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.exception.ServiceException;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.domain.SysUser;
import com.example.aicodehelper.mapper.SysUserMapper;
import com.example.aicodehelper.security.TokenService;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务：账号密码校验、签发令牌、装载登录用户
 */
@Service
public class SysLoginService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysPermissionService sysPermissionService;

    @Resource
    private TokenService tokenService;

    /**
     * 账号密码登录，成功后返回 JWT 令牌
     */
    public String login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUser_name, username));
        if (user == null) {
            throw new ServiceException("用户不存在或密码错误");
        }
        if (!Constants.DEL_FLAG_NORMAL.equals(user.getDel_flag())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (!Constants.STATUS_NORMAL.equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            throw new ServiceException("用户不存在或密码错误");
        }
        LoginUser loginUser = buildLoginUser(user);
        return tokenService.createToken(loginUser);
    }

    /**
     * 根据用户ID装载登录用户上下文（拦截器每次请求调用），用户无效时返回 null
     */
    public LoginUser loadLoginUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null
                || !Constants.DEL_FLAG_NORMAL.equals(user.getDel_flag())
                || !Constants.STATUS_NORMAL.equals(user.getStatus())) {
            return null;
        }
        return buildLoginUser(user);
    }

    /**
     * 装配登录用户：角色标识 + 菜单权限
     */
    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUser_id());
        loginUser.setUserName(user.getUser_name());
        loginUser.setUser(user);
        Set<String> roles = sysPermissionService.getRolePermission(user);
        loginUser.setRoles(roles);
        loginUser.setPermissions(sysPermissionService.getMenuPermission(user, roles));
        return loginUser;
    }
}
