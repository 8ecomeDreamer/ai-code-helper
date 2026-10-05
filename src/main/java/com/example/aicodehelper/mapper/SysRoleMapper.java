package com.example.aicodehelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aicodehelper.domain.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author Jim
* @description 针对表【sys_role(角色信息表)】的数据库操作Mapper
* @Entity com.example.aicodehelper.domain.SysRole
*/
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询其拥有的角色
     */
    @Select("select r.* from sys_role r "
            + "left join sys_user_role ur on r.role_id = ur.role_id "
            + "where ur.user_id = #{userId} and r.del_flag = '0'")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
