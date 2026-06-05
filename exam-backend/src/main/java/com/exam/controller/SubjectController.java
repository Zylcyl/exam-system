package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectMapper subjectMapper;

    @GetMapping("/list")
    public Result<List<Subject>> list() {
        List<Subject> list = subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>().orderByAsc(Subject::getSortOrder)
        );
        return Result.success(list);
    }

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<Subject> all = subjectMapper.selectList(
                new LambdaQueryWrapper<Subject>().orderByAsc(Subject::getSortOrder)
        );
        return Result.success(buildTree(all, 0L));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> create(@RequestBody Subject subject) {
        subjectMapper.insert(subject);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> update(@PathVariable Long id, @RequestBody Subject subject) {
        subject.setId(id);
        subjectMapper.updateById(subject);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> delete(@PathVariable Long id) {
        subjectMapper.deleteById(id);
        return Result.success("删除成功");
    }

    private List<Map<String, Object>> buildTree(List<Subject> list, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Subject s : list) {
            if (s.getParentId().equals(parentId)) {
                Map<String, Object> node = new java.util.LinkedHashMap<>();
                node.put("id", s.getId());
                node.put("label", s.getSubjectName());
                node.put("parentId", s.getParentId());
                node.put("description", s.getDescription());
                List<Map<String, Object>> children = buildTree(list, s.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                tree.add(node);
            }
        }
        return tree;
    }
}
