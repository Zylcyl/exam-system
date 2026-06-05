package com.exam.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.SysUser;
import com.exam.mapper.SysUserMapper;
import com.exam.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public Page<SysUser> pageUsers(int page, int size, String keyword, Long roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        if (roleId != null) {
            wrapper.eq(SysUser::getRoleId, roleId);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public void createUser(SysUser user) {
        // 检查用户名唯一性
        SysUser exist = getByUsername(user.getUsername());
        if (exist != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
    }

    @Override
    public void updateUser(SysUser user) {
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        this.updateById(user);
    }

    @Override
    @Transactional
    public Map<String, Integer> importStudents(MultipartFile file) {
        int success = 0, fail = 0;
        try (InputStream is = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(is);
            reader.addHeaderAlias("用户名", "username");
            reader.addHeaderAlias("姓名", "realName");
            reader.addHeaderAlias("密码", "password");
            reader.addHeaderAlias("邮箱", "email");
            reader.addHeaderAlias("手机号", "phone");

            List<SysUser> users = reader.readAll(SysUser.class);
            for (SysUser user : users) {
                try {
                    if (StrUtil.isBlank(user.getUsername())) {
                        fail++;
                        continue;
                    }
                    if (getByUsername(user.getUsername()) != null) {
                        fail++;
                        continue;
                    }
                    user.setRoleId(3L); // 学生角色
                    user.setPassword(passwordEncoder.encode(
                            StrUtil.isBlank(user.getPassword()) ? "123456" : user.getPassword()
                    ));
                    user.setStatus(1);
                    this.save(user);
                    success++;
                } catch (Exception e) {
                    fail++;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel 解析失败: " + e.getMessage());
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return result;
    }
}
