package com.example.aicodehelper.controller;

import com.example.aicodehelper.common.core.Result;
import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.utils.SecurityUtils;
import com.example.aicodehelper.domain.dto.LoginBody;
import com.example.aicodehelper.domain.vo.RouterVo;
import com.example.aicodehelper.service.SysLoginService;
import com.example.aicodehelper.service.SysMenuService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证与用户信息接口：登录、退出、用户信息、动态路由
 */
@RestController
public class SysLoginController {

    @Resource
    private SysLoginService sysLoginService;

    @Resource
    private SysMenuService sysMenuService;

    /**
     * 登录：成功后返回 JWT 令牌
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginBody loginBody) {
        String token = sysLoginService.login(loginBody.getUsername(), loginBody.getPassword());
        return Result.success("登录成功").put("token", token);
    }

    /**
     * 退出登录（无状态令牌，客户端删除令牌即可）
     */
    @PostMapping("/logout")
    public Result logout() {
        return Result.success("退出成功");
    }

    /**
     * 获取当前登录用户信息：用户 + 角色 + 权限
     */
    @GetMapping("/getInfo")
    public Result getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return Result.success()
                .put("user", loginUser.getUser())
                .put("roles", loginUser.getRoles())
                .put("permissions", loginUser.getPermissions());
    }

    /**
     * 获取当前用户的动态路由
     */
    @GetMapping("/getRouters")
    public Result getRouters() {
        List<RouterVo> routers = sysMenuService.buildMenus(sysMenuService.selectMenuTreeByUserId());
        return Result.success(routers);
    }
}
