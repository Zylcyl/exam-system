package com.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Result;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final ExamScoreMapper examScoreMapper;
    private final ExamStudentMapper examStudentMapper;
    private final ExamMapper examMapper;
    private final SysUserMapper userMapper;
    private final StudentAnswerMapper studentAnswerMapper;

    /**
     * 教师端成绩列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Page<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long examId) {
        LambdaQueryWrapper<ExamStudent> wrapper = new LambdaQueryWrapper<ExamStudent>()
                .eq(ExamStudent::getExamId, examId)
                .orderByDesc(ExamStudent::getTotalScore);
        Page<ExamStudent> pageResult = examStudentMapper.selectPage(new Page<>(page, size), wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ExamStudent es : pageResult.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", es.getId());
            map.put("examId", es.getExamId());
            map.put("studentId", es.getStudentId());
            SysUser student = userMapper.selectById(es.getStudentId());
            map.put("studentName", student != null ? student.getRealName() : "未知");
            map.put("studentUsername", student != null ? student.getUsername() : "");
            map.put("status", es.getStatus());
            map.put("totalScore", es.getTotalScore());
            map.put("cheatCount", es.getCheatCount());
            map.put("isMarked", es.getIsMarked());
            map.put("submitTime", es.getSubmitTime());
            map.put("startTime", es.getStartTime());
            list.add(map);
        }

        Page<Map<String, Object>> dtoPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }

    /**
     * 查看某个学生答卷详情
     */
    @GetMapping("/detail")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<List<Map<String, Object>>> detail(@RequestParam Long examId, @RequestParam Long studentId) {
        List<StudentAnswer> answers = studentAnswerMapper.findByExamAndStudent(examId, studentId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (StudentAnswer sa : answers) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", sa.getId());
            map.put("questionId", sa.getQuestionId());
            map.put("answerContent", sa.getAnswerContent());
            map.put("isCorrect", sa.getIsCorrect());
            map.put("score", sa.getScore());
            map.put("markComment", sa.getMarkComment());
            list.add(map);
        }
        return Result.success(list);
    }

    /**
     * 主观题阅卷
     */
    @PutMapping("/mark")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<?> markAnswer(@RequestBody Map<String, Object> body, @AuthenticationPrincipal UserPrincipal principal) {
        Long answerId = Long.valueOf(body.get("answerId").toString());
        Integer score = Integer.valueOf(body.get("score").toString());
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";

        StudentAnswer sa = studentAnswerMapper.selectById(answerId);
        if (sa == null) return Result.error("答题记录不存在");

        sa.setScore(score);
        sa.setIsCorrect(score > 0 ? 1 : 0);
        sa.setMarkedBy(principal.getUserId());
        sa.setMarkComment(comment);
        studentAnswerMapper.updateById(sa);

        // 更新考生总分
        recalcTotalScore(sa.getExamId(), sa.getStudentId());

        return Result.success("评分成功");
    }

    private void recalcTotalScore(Long examId, Long studentId) {
        List<StudentAnswer> answers = studentAnswerMapper.findByExamAndStudent(examId, studentId);
        int total = answers.stream().filter(a -> a.getScore() != null).mapToInt(StudentAnswer::getScore).sum();
        ExamStudent es = examStudentMapper.selectOne(
                new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getExamId, examId)
                        .eq(ExamStudent::getStudentId, studentId)
        );
        if (es != null) {
            es.setTotalScore(total);
            es.setIsMarked(1);
            examStudentMapper.updateById(es);
        }

        // 更新 exam_score
        ExamScore score = examScoreMapper.selectOne(
                new LambdaQueryWrapper<ExamScore>().eq(ExamScore::getExamId, examId)
                        .eq(ExamScore::getStudentId, studentId)
        );
        if (score != null) {
            score.setTotalScore(total);
            score.setIsPassed(total >= 60 ? 1 : 0);
            examScoreMapper.updateById(score);
        }
    }

    /**
     * 统计概览
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Result<Map<String, Object>> stats(@RequestParam Long examId) {
        List<ExamStudent> list = examStudentMapper.selectList(
                new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getExamId, examId)
        );

        int total = list.size();
        int finished = (int) list.stream().filter(s -> "FINISHED".equals(s.getStatus())).count();
        int passed = (int) list.stream().filter(s -> s.getTotalScore() != null && s.getTotalScore() >= 60).count();
        double avgScore = list.stream().filter(s -> s.getTotalScore() != null).mapToInt(ExamStudent::getTotalScore).average().orElse(0);

        // 分数分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", 0); distribution.put("60-69", 0); distribution.put("70-79", 0);
        distribution.put("80-89", 0); distribution.put("90-100", 0);
        for (ExamStudent s : list) {
            if (s.getTotalScore() == null) continue;
            int score = s.getTotalScore();
            if (score < 60) distribution.merge("0-59", 1, Integer::sum);
            else if (score < 70) distribution.merge("60-69", 1, Integer::sum);
            else if (score < 80) distribution.merge("70-79", 1, Integer::sum);
            else if (score < 90) distribution.merge("80-89", 1, Integer::sum);
            else distribution.merge("90-100", 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("finished", finished);
        result.put("passed", passed);
        result.put("avgScore", Math.round(avgScore * 10) / 10.0);
        result.put("distribution", distribution);
        return Result.success(result);
    }

    /**
     * 学生查看自己的成绩
     */
    @GetMapping("/my-scores")
    public Result<List<Map<String, Object>>> myScores(@AuthenticationPrincipal UserPrincipal principal) {
        List<ExamStudent> list = examStudentMapper.selectList(
                new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getStudentId, principal.getUserId())
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (ExamStudent es : list) {
            Exam exam = examMapper.selectById(es.getExamId());
            if (exam == null) continue;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("examId", exam.getId());
            map.put("examName", exam.getExamName());
            map.put("totalScore", exam.getTotalScore());
            map.put("myScore", es.getTotalScore());
            map.put("passScore", exam.getPassScore());
            map.put("isPassed", es.getTotalScore() != null && es.getTotalScore() >= exam.getPassScore());
            map.put("submitTime", es.getSubmitTime());
            result.add(map);
        }
        return Result.success(result);
    }
}
