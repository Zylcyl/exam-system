package com.exam.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.annotation.OperateLog;
import com.exam.common.Result;
import com.exam.entity.SysUser;
import com.exam.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserManageController {

    private final SysUserService userService;

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId) {
        return Result.success(userService.pageUsers(page, size, keyword, roleId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    @OperateLog("创建用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@RequestBody SysUser user) {
        userService.createUser(user);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @OperateLog("更新用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @OperateLog("删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Integer>> importStudents(@RequestParam("file") MultipartFile file) {
        return Result.success("导入完成", userService.importStudents(file));
    }

    @GetMapping("/export-template")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("realName", "姓名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "手机号");
        writer.setOnlyAlias(true);

        // 示例数据
        writer.write(List.of(
                new String[]{"zhangsan", "张三", "123456", "zhangsan@example.com", "13800138001"},
                new String[]{"lisi", "李四", "123456", "lisi@example.com", "13800138002"}
        ));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生导入模板", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
}
