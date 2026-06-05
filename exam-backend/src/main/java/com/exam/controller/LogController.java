package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.SysLog;
import com.exam.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/log")
@RequiredArgsConstructor
public class LogController {

    private final SysLogMapper sysLogMapper;

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysLog::getOperation, keyword)
                    .or().like(SysLog::getUsername, keyword);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        return Result.success(sysLogMapper.selectPage(new Page<>(page, size), wrapper));
    }
}
