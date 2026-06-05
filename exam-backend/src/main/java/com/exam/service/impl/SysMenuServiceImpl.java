package com.exam.service.impl;

import com.exam.entity.SysMenu;
import com.exam.mapper.SysMenuMapper;
import com.exam.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<Map<String, Object>> getMenusByRoleCode(String roleCode) {
        List<SysMenu> allMenus = menuMapper.selectMenusByRoleCode(roleCode);
        return buildMenuTree(allMenus, 0L);
    }

    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menuList, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu menu : menuList) {
            if (menu.getParentId().equals(parentId)) {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", menu.getId());
                node.put("parentId", menu.getParentId());
                node.put("name", menu.getMenuName());
                node.put("type", menu.getMenuType());
                node.put("path", menu.getPath());
                node.put("component", menu.getComponent());
                node.put("permission", menu.getPermission());
                node.put("icon", menu.getIcon());
                node.put("sortOrder", menu.getSortOrder());

                List<Map<String, Object>> children = buildMenuTree(menuList, menu.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                tree.add(node);
            }
        }
        return tree;
    }
}
