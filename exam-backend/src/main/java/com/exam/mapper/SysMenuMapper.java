package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_role r ON rm.role_id = r.id " +
            "WHERE r.role_code = #{roleCode} AND m.status = 1 " +
            "ORDER BY m.sort_order")
    List<SysMenu> selectMenusByRoleCode(String roleCode);
}
