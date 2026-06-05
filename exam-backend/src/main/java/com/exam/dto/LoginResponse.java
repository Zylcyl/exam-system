package com.exam.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String roleName;
}
