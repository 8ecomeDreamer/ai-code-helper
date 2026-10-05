package com.example.aicodehelper.common.utils;

import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.core.LoginUser;
import com.example.aicodehelper.common.exception.ServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 安全工具类：当前登录用户上下文 + 密码加密
 */
public class SecurityUtils {

    private static final ThreadLocal<LoginUser> LOGIN_USER_HOLDER = new ThreadLocal<>();

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private SecurityUtils() {
    }

    public static void setLoginUser(LoginUser loginUser) {
        LOGIN_USER_HOLDER.set(loginUser);
    }

    /**
     * 获取当前登录用户，未登录时抛出 401 业务异常
     */
    public static LoginUser getLoginUser() {
        LoginUser loginUser = LOGIN_USER_HOLDER.get();
        if (loginUser == null) {
            throw new ServiceException(Constants.UNAUTHORIZED, "登录状态已过期或访问未授权，请重新登录");
        }
        return loginUser;
    }

    public static void clear() {
        LOGIN_USER_HOLDER.remove();
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getUsername() {
        return getLoginUser().getUserName();
    }

    /**
     * 当前登录用户是否为超级管理员
     */
    public static boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser.getRoles() != null && loginUser.getRoles().contains(Constants.ADMIN_ROLE_KEY);
    }

    /** 生成 BCrypt 加密密码 */
    public static String encryptPassword(String password) {
        return PASSWORD_ENCODER.encode(password);
    }

    /** 校验明文密码与加密密码是否匹配 */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return rawPassword != null && encodedPassword != null
                && PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
