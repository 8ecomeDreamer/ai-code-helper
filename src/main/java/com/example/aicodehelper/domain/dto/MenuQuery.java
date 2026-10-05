package com.example.aicodehelper.domain.dto;

import lombok.Data;

/**
 * 菜单列表查询条件（JSON 契约为 snake_case，与实体风格一致）
 */
@Data
public class MenuQuery {

    /** 菜单名称（模糊匹配） */
    private String menu_name;

    /** 菜单状态（0正常 1停用） */
    private String status;
}
