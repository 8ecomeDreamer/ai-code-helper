package com.example.aicodehelper.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

/**
 * 动态路由配置（若依风格）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {

    /** 路由名称 */
    private String name;

    /** 路由地址 */
    private String path;

    /** 是否隐藏路由 */
    private Boolean hidden;

    /** 重定向地址 */
    private String redirect;

    /** 组件地址 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 是否总是显示（目录只有一个子路由时） */
    private Boolean alwaysShow;

    /** 路由元数据 */
    private MetaVo meta;

    /** 子路由 */
    private List<RouterVo> children;
}
