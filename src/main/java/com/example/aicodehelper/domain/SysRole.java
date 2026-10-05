package com.example.aicodehelper.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色信息表
 * @TableName sys_role
 */
@TableName(value ="sys_role")
@Data
public class SysRole implements Serializable {
    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long role_id;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String role_name;

    /**
     * 角色权限字符串
     */
    @TableField(value = "role_key")
    private String role_key;

    /**
     * 显示顺序
     */
    @TableField(value = "role_sort")
    private Integer role_sort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）
     */
    @TableField(value = "data_scope")
    private String data_scope;

    /**
     * 菜单树选择项是否关联显示
     */
    @TableField(value = "menu_check_strictly")
    private Integer menu_check_strictly;

    /**
     * 部门树选择项是否关联显示
     */
    @TableField(value = "dept_check_strictly")
    private Integer dept_check_strictly;

    /**
     * 角色状态（0正常 1停用）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @TableField(value = "del_flag")
    private String del_flag;

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