package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.entity.SysUser;
import com.exam.security.JwtTokenProvider;
import com.exam.security.UserPrincipal;
import com.exam.service.SysMenuService;
import com.exam.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysUserService userService;
    private final SysMenuService menuService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal.getUserId(), principal.getUsername(), principal.getRoleCode());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(principal.getUserId())
                .username(principal.getUsername())
                .realName(principal.getRealName())
                .roleCode(principal.getRoleCode())
                .build();

        return Result.success("登录成功", response);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", principal.getUserId());
        data.put("username", principal.getUsername());
        data.put("realName", principal.getRealName());
        data.put("roleCode", principal.getRoleCode());

        // 获取用户菜单
        List<?> menus = menuService.getMenusByRoleCode(principal.getRoleCode());
        data.put("menus", menus);

        return Result.success(data);
    }
}
