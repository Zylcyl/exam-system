package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.exam.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Long roleId;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String roleName;

    @TableField(exist = false)
    private String roleCode;
}
