package com.example.aicodehelper.common.exception;

import lombok.Getter;

/**
 * 无访问权限异常
 */
@Getter
public class NotPermissionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 被拦截的权限标识 */
    private final String permission;

    public NotPermissionException(String permission) {
        super("没有访问权限：" + permission);
        this.permission = permission;
    }
}
