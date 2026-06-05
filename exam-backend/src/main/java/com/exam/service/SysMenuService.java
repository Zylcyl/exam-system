package com.exam.service;

import java.util.List;

public interface SysMenuService {
    List<?> getMenusByRoleCode(String roleCode);
}
