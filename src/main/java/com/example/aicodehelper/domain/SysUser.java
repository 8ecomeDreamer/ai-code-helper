package com.example.aicodehelper.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户信息表
 * @TableName sys_user
 */
@TableName(value = "sys_user")
@Data
public class SysUser implements Serializable {

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long user_id;

    /**
     * 部门ID
     */
    @TableField(value = "dept_id")
    private Long dept_id;

    /**
     * 用户账号
     */
    @TableField(value = "user_name")
    private String user_name;

    /**
     * 用户昵称
     */
    @TableField(value = "nick_name")
    private String nick_name;

    /**
     * 用户类型（00系统用户）
     */
    @TableField(value = "user_type")
    private String user_type;

    /**
     * AI用户类型：1正式用户 2体验访客guest
     */
    @TableField(value = "agent_type")
    private String agent_type;

    /**
     * 用户邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 手机号码
     */
    @TableField(value = "phonenumber")
    private String phonenumber;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @TableField(value = "sex")
    private String sex;

    /**
     * 头像地址
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 密码（不对外输出）
     */
    @JsonIgnore
    @TableField(value = "password")
    private String password;

    /**
     * 账号状态（0正常 1停用）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableField(value = "del_flag")
    private String del_flag;

    /**
     * 最后登录IP
     */
    @TableField(value = "login_ip")
    private String login_ip;

    /**
     * 最后登录时间
     */
    @TableField(value = "login_date")
    private Date login_date;

    /**
     * 密码最后更新时间
     */
    @TableField(value = "pwd_update_date")
    private Date pwd_update_date;

    /**
     * 创建者
     */
    @TableField(value = "create_by")
    private String create_by;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    /**
     * 更新者
     */
    @TableField(value = "update_by")
    private String update_by;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date update_time;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
