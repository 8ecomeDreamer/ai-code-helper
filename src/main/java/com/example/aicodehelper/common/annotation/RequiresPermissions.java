package com.example.aicodehelper.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解：标注在控制器方法上，校验当前登录用户是否拥有指定权限标识
 * 多个权限标识为"与"关系，超级管理员（admin）自动放行
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {

    /** 需要校验的权限标识，如 system:menu:list */
    String[] value();
}
