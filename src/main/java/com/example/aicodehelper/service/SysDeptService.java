package com.example.aicodehelper.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.example.aicodehelper.domain.SysDept;
import com.example.aicodehelper.domain.vo.TreeSelect;
import java.util.List;

/**
* @author Jim
* @description 针对表【sys_dept(部门表)】的数据库操作Service
*/
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询部门列表（平铺，按父ID+显示顺序排序）
     */
    List<SysDept> selectDeptList(SysDept query);

    /**
     * 构建部门树结构
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);

    /**
     * 构建前端树控件结构（id/label/children）
     */
    List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

    /**
     * 是否存在子部门
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 部门下是否存在用户
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 同一父级下部门名称是否唯一
     */
    boolean checkDeptNameUnique(SysDept dept);

    /**
     * 校验父部门有效性，返回父部门（顶级部门父ID为0时返回 null）
     */
    SysDept checkParentDept(Long parentId);

    /**
     * 根据父部门计算祖级列表：父祖级 + 父ID
     */
    String buildAncestors(SysDept parent);

    /**
     * 父部门变更时同步修正子孙部门的祖级列表前缀
     */
    void updateDeptChildren(Long deptId, String oldAncestors, String newAncestors);
}
