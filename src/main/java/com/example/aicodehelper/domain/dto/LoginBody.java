package com.example.aicodehelper.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求体
 */
@Data
public class LoginBody {

    /** 用户账号 */
    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过30个字符")
    private String username;

    /** 用户密码 */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = 5, max = 20, message = "用户密码长度必须介于5和20之间")
    private String password;
}
