package com.example.aicodehelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.common.constant.Constants;
import com.example.aicodehelper.common.exception.ServiceException;
import com.example.aicodehelper.domain.SysDept;
import com.example.aicodehelper.domain.SysUser;
import com.example.aicodehelper.domain.vo.TreeSelect;
import com.example.aicodehelper.mapper.SysDeptMapper;
import com.example.aicodehelper.mapper.SysUserMapper;
import com.example.aicodehelper.service.SysDeptService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
* @author Jim
* @description 针对表【sys_dept(部门表)】的数据库操作Service实现
*/
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
    implements SysDeptService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public List<SysDept> selectDeptList(SysDept query) {
        return baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDel_flag, Constants.DEL_FLAG_NORMAL)
                .like(query != null && StringUtils.hasText(query.getDept_name()),
                        SysDept::getDept_name, query == null ? null : query.getDept_name())
                .eq(query != null && StringUtils.hasText(query.getStatus()),
                        SysDept::getStatus, query == null ? null : query.getStatus())
                .orderByAsc(SysDept::getParent_id)
                .orderByAsc(SysDept::getOrder_num));
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        Set<Long> ids = depts.stream().map(SysDept::getDept_id).collect(Collectors.toSet());
        // 根节点：父ID不在结果集中的部门
        List<SysDept> roots = depts.stream()
                .filter(d -> d.getParent_id() == null || !ids.contains(d.getParent_id()))
                .collect(Collectors.toList());
        roots.forEach(root -> root.setChildren(getChildDepts(root, depts)));
        return roots;
    }

    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts) {
        return buildDeptTree(depts).stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParent_id, deptId)
                .eq(SysDept::getDel_flag, Constants.DEL_FLAG_NORMAL)) > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDept_id, deptId)
                .eq(SysUser::getDel_flag, Constants.DEL_FLAG_NORMAL)) > 0;
    }

    @Override
    public boolean checkDeptNameUnique(SysDept dept) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDept_name, dept.getDept_name())
                .eq(SysDept::getParent_id, dept.getParent_id())
                .eq(SysDept::getDel_flag, Constants.DEL_FLAG_NORMAL)
                .ne(dept.getDept_id() != null, SysDept::getDept_id, dept.getDept_id())) == 0;
    }

    /**
     * 新增部门时根据父部门计算祖级列表：父祖级 + 父ID
     */
    @Override
    public String buildAncestors(SysDept parent) {
        if (parent == null) {
            return "0";
        }
        return StringUtils.hasText(parent.getAncestors())
                ? parent.getAncestors() + "," + parent.getDept_id()
                : String.valueOf(parent.getDept_id());
    }

    /**
     * 父部门变更时同步修正子孙部门的祖级列表前缀
     */
    @Override
    public void updateDeptChildren(Long deptId, String oldAncestors, String newAncestors) {
        String oldPrefix = oldAncestors + "," + deptId;
        String newPrefix = newAncestors + "," + deptId;
        List<SysDept> children = baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .likeRight(SysDept::getAncestors, oldPrefix));
        for (SysDept child : children) {
            child.setAncestors(newPrefix + child.getAncestors().substring(oldPrefix.length()));
            baseMapper.updateById(child);
        }
    }

    /** 校验父部门有效性，返回父部门（顶级部门父ID为0时返回 null） */
    @Override
    public SysDept checkParentDept(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }
        SysDept parent = baseMapper.selectById(parentId);
        if (parent == null || !Constants.DEL_FLAG_NORMAL.equals(parent.getDel_flag())) {
            throw new ServiceException("上级部门不存在，无法保存");
        }
        return parent;
    }

    private List<SysDept> getChildDepts(SysDept parent, List<SysDept> depts) {
        List<SysDept> children = depts.stream()
                .filter(d -> parent.getDept_id().equals(d.getParent_id()))
                .collect(Collectors.toList());
        children.forEach(child -> child.setChildren(getChildDepts(child, depts)));
        return children;
    }
}
