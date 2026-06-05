package com.exam.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.security.UserPrincipal;
import com.exam.service.QuestionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Page<QuestionDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {
        return Result.success(questionService.pageQuestions(page, size, subjectId, questionType, difficulty, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<QuestionDTO> getById(@PathVariable Long id) {
        Question q = questionService.getById(id);
        if (q == null) return Result.error("题目不存在");
        return Result.success(questionService.pageQuestions(1, 1, null, null, null, null)
                .getRecords().stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> create(@RequestBody QuestionDTO dto, @AuthenticationPrincipal UserPrincipal principal) {
        dto.setCreateBy(principal.getUserId());
        questionService.createQuestion(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> update(@PathVariable Long id, @RequestBody QuestionDTO dto) {
        dto.setId(id);
        questionService.updateQuestion(dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success("删除成功");
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Map<String, Integer>> importQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long subjectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success("导入完成", questionService.importQuestions(file, subjectId, principal.getUserId()));
    }

    @GetMapping("/export-template")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.addHeaderAlias("questionType", "题型");
        writer.addHeaderAlias("title", "题目内容");
        writer.addHeaderAlias("difficulty", "难度");
        writer.addHeaderAlias("score", "分值");
        writer.addHeaderAlias("optionA", "选项A");
        writer.addHeaderAlias("optionB", "选项B");
        writer.addHeaderAlias("optionC", "选项C");
        writer.addHeaderAlias("optionD", "选项D");
        writer.addHeaderAlias("correctAnswer", "正确答案");
        writer.addHeaderAlias("analysis", "解析");
        writer.setOnlyAlias(true);

        writer.write(List.of(
                new String[]{"单选题", "Java中int的默认值是多少？", "简单", "5", "0", "null", "false", "1", "A", "int默认值为0"},
                new String[]{"多选题", "以下哪些是Java关键字？", "中等", "10", "class", "void", "main", "string", "AB", "main和string不是关键字"}
        ));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("题目导入模板", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
}
