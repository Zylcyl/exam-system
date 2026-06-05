package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.SysUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);
    Page<SysUser> pageUsers(int page, int size, String keyword, Long roleId);
    void createUser(SysUser user);
    void updateUser(SysUser user);
    Map<String, Integer> importStudents(MultipartFile file);
}
