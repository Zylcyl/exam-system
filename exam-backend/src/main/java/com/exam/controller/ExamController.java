package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.dto.ExamDTO;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Exam;
import com.exam.entity.ExamQuestion;
import com.exam.entity.ExamStudent;
import com.exam.entity.SysUser;
import com.exam.mapper.ExamQuestionMapper;
import com.exam.mapper.ExamStudentMapper;
import com.exam.mapper.SysUserMapper;
import com.exam.security.UserPrincipal;
import com.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamStudentMapper examStudentMapper;
    private final SysUserMapper userMapper;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Page<ExamDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long subjectId) {
        return Result.success(examService.pageExams(page, size, keyword, status, subjectId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<ExamDTO> getById(@PathVariable Long id) {
        ExamDTO dto = examService.getExamDetail(id);
        if (dto == null) return Result.error("考试不存在");
        // 填充已选题目ID和考生ID
        List<ExamQuestion> eqList = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, id)
        );
        dto.setQuestionIds(eqList.stream().map(ExamQuestion::getQuestionId).collect(java.util.stream.Collectors.toList()));
        List<ExamStudent> esList = examStudentMapper.selectList(
                new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getExamId, id)
        );
        dto.setStudentIds(esList.stream().map(ExamStudent::getStudentId).collect(java.util.stream.Collectors.toList()));
        return Result.success(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> create(@RequestBody ExamDTO dto, @AuthenticationPrincipal UserPrincipal principal) {
        dto.setCreateBy(principal.getUserId());
        examService.createExam(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> update(@PathVariable Long id, @RequestBody ExamDTO dto) {
        dto.setId(id);
        examService.updateExam(dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> delete(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.success("删除成功");
    }

    /**
     * 获取学生列表（用于考试分配）
     */
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<SysUser>> getStudents() {
        List<SysUser> students = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, 3L)
        );
        return Result.success(students);
    }

    // ==================== 学生端 ====================

    /**
     * 学生考试列表
     */
    @GetMapping("/my-exams")
    public Result<List<Map<String, Object>>> myExams(@AuthenticationPrincipal UserPrincipal principal) {
        List<ExamStudent> myList = examStudentMapper.selectList(
                new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getStudentId, principal.getUserId())
        );

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ExamStudent es : myList) {
            Exam exam = examService.getById(es.getExamId());
            if (exam == null) continue;
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("examId", exam.getId());
            map.put("examName", exam.getExamName());
            map.put("startTime", exam.getStartTime());
            map.put("endTime", exam.getEndTime());
            map.put("duration", exam.getDuration());
            map.put("totalScore", exam.getTotalScore());
            map.put("passScore", exam.getPassScore());
            map.put("examStatus", exam.getStatus());
            map.put("myStatus", es.getStatus());
            map.put("myScore", es.getTotalScore());
            map.put("submitTime", es.getSubmitTime());
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 开始考试
     */
    @GetMapping("/{examId}/start")
    public Result<List<QuestionDTO>> startExam(@PathVariable Long examId, @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(examService.startExam(examId, principal.getUserId()));
    }

    /**
     * 保存答案
     */
    @PostMapping("/{examId}/answer")
    public Result<?> saveAnswer(@PathVariable Long examId,
                                 @RequestParam Long questionId,
                                 @RequestParam String answerContent,
                                 @AuthenticationPrincipal UserPrincipal principal) {
        examService.saveAnswer(examId, principal.getUserId(), questionId, answerContent);
        return Result.success("保存成功");
    }

    /**
     * 切屏检测
     */
    @PostMapping("/{examId}/cheat")
    public Result<Integer> recordCheat(@PathVariable Long examId, @AuthenticationPrincipal UserPrincipal principal) {
        int count = examService.recordCheat(examId, principal.getUserId());
        return Result.success(count);
    }

    /**
     * 提交考试
     */
    @PostMapping("/{examId}/submit")
    public Result<Map<String, Object>> submitExam(@PathVariable Long examId, @AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(examService.submitExam(examId, principal.getUserId()));
    }
}
