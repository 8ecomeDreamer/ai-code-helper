package com.example.aicodehelper.domain.vo;

import com.example.aicodehelper.domain.SysDept;
import com.example.aicodehelper.domain.SysMenu;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * 树结构下拉选择（菜单树）
 */
@Data
public class TreeSelect {

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 子节点 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect() {
    }

    public TreeSelect(SysMenu menu) {
        this.id = menu.getMenu_id();
        this.label = menu.getMenu_name();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public TreeSelect(SysDept dept) {
        this.id = dept.getDept_id();
        this.label = dept.getDept_name();
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }
}
