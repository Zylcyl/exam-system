package com.exam.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.ExamDTO;
import com.exam.dto.QuestionDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService extends ServiceImpl<ExamMapper, Exam> {

    private final ExamQuestionMapper examQuestionMapper;
    private final ExamStudentMapper examStudentMapper;
    private final QuestionService questionService;
    private final SubjectMapper subjectMapper;
    private final SysUserMapper userMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamScoreMapper examScoreMapper;

    public Page<ExamDTO> pageExams(int page, int size, String keyword, String status, Long subjectId) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Exam::getExamName, keyword);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Exam::getStatus, status);
        }
        if (subjectId != null) {
            wrapper.eq(Exam::getSubjectId, subjectId);
        }
        wrapper.orderByDesc(Exam::getCreateTime);

        Page<Exam> pageResult = this.page(new Page<>(page, size), wrapper);
        List<ExamDTO> dtoList = pageResult.getRecords().stream().map(e -> {
            ExamDTO dto = new ExamDTO();
            copyProps(e, dto);
            Subject sub = subjectMapper.selectById(e.getSubjectId());
            if (sub != null) dto.setSubjectName(sub.getSubjectName());
            // 统计题目数
            Long qCount = examQuestionMapper.selectCount(
                    new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, e.getId())
            );
            dto.setQuestionCount(qCount.intValue());
            // 统计考生数
            Long sCount = examStudentMapper.selectCount(
                    new LambdaQueryWrapper<ExamStudent>().eq(ExamStudent::getExamId, e.getId())
            );
            dto.setStudentCount(sCount.intValue());
            return dto;
        }).collect(Collectors.toList());

        Page<ExamDTO> dtoPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    public ExamDTO getExamDetail(Long examId) {
        Exam exam = this.getById(examId);
        if (exam == null) return null;
        ExamDTO dto = new ExamDTO();
        copyProps(exam, dto);
        Subject sub = subjectMapper.selectById(exam.getSubjectId());
        if (sub != null) dto.setSubjectName(sub.getSubjectName());
        return dto;
    }

    @Transactional
    public void createExam(ExamDTO dto) {
        Exam exam = new Exam();
        exam.setExamName(dto.getExamName());
        exam.setSubjectId(dto.getSubjectId());
        exam.setDescription(dto.getDescription());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setDuration(dto.getDuration());
        exam.setTotalScore(dto.getTotalScore() != null ? dto.getTotalScore() : 100);
        exam.setPassScore(dto.getPassScore() != null ? dto.getPassScore() : 60);
        exam.setStatus("NOT_START");
        exam.setIsRandomOrder(dto.getIsRandomOrder() != null ? dto.getIsRandomOrder() : 1);
        exam.setAllowRetry(dto.getAllowRetry() != null ? dto.getAllowRetry() : 0);
        exam.setMaxCheatCount(dto.getMaxCheatCount() != null ? dto.getMaxCheatCount() : 3);
        exam.setCreateBy(dto.getCreateBy());
        this.save(exam);

        // 关联题目
        if (dto.getQuestionIds() != null) {
            int order = 0;
            for (Long qid : dto.getQuestionIds()) {
                Question q = questionService.getById(qid);
                ExamQuestion eq = new ExamQuestion();
                eq.setExamId(exam.getId());
                eq.setQuestionId(qid);
                eq.setQuestionOrder(order++);
                eq.setScore(q != null ? q.getScore() : 5);
                examQuestionMapper.insert(eq);
            }
        }

        // 关联考生
        if (dto.getStudentIds() != null) {
            for (Long sid : dto.getStudentIds()) {
                ExamStudent es = new ExamStudent();
                es.setExamId(exam.getId());
                es.setStudentId(sid);
                es.setStatus("WAITING");
                examStudentMapper.insert(es);
            }
        }
    }

    @Transactional
    public void updateExam(ExamDTO dto) {
        Exam exam = new Exam();
        exam.setId(dto.getId());
        exam.setExamName(dto.getExamName());
        exam.setSubjectId(dto.getSubjectId());
        exam.setDescription(dto.getDescription());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setDuration(dto.getDuration());
        exam.setTotalScore(dto.getTotalScore());
        exam.setPassScore(dto.getPassScore());
        exam.setIsRandomOrder(dto.getIsRandomOrder());
        exam.setAllowRetry(dto.getAllowRetry());
        exam.setMaxCheatCount(dto.getMaxCheatCount());
        this.updateById(exam);

        // 更新题目关联
        if (dto.getQuestionIds() != null) {
            examQuestionMapper.deleteByExamId(dto.getId());
            int order = 0;
            for (Long qid : dto.getQuestionIds()) {
                Question q = questionService.getById(qid);
                ExamQuestion eq = new ExamQuestion();
                eq.setExamId(dto.getId());
                eq.setQuestionId(qid);
                eq.setQuestionOrder(order++);
                eq.setScore(q != null ? q.getScore() : 5);
                examQuestionMapper.insert(eq);
            }
        }

        // 更新考生关联
        if (dto.getStudentIds() != null) {
            examStudentMapper.deleteByExamId(dto.getId());
            for (Long sid : dto.getStudentIds()) {
                ExamStudent es = new ExamStudent();
                es.setExamId(dto.getId());
                es.setStudentId(sid);
                es.setStatus("WAITING");
                examStudentMapper.insert(es);
            }
        }
    }

    @Transactional
    public void deleteExam(Long id) {
        examQuestionMapper.deleteByExamId(id);
        examStudentMapper.deleteByExamId(id);
        this.removeById(id);
    }

    /**
     * 学生开始考试
     */
    @Transactional
    public List<QuestionDTO> startExam(Long examId, Long studentId) {
        Exam exam = this.getById(examId);
        if (exam == null) throw new IllegalArgumentException("考试不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) throw new IllegalArgumentException("考试尚未开始");
        if (now.isAfter(exam.getEndTime())) throw new IllegalArgumentException("考试已结束");

        ExamStudent es = examStudentMapper.selectOne(
                new LambdaQueryWrapper<ExamStudent>()
                        .eq(ExamStudent::getExamId, examId)
                        .eq(ExamStudent::getStudentId, studentId)
        );
        if (es == null) throw new IllegalArgumentException("您不是本次考试的考生");

        if ("FINISHED".equals(es.getStatus())) {
            if (exam.getAllowRetry() == 0) throw new IllegalArgumentException("您已完成本次考试，不允许重考");
        }

        // 更新考生状态
        es.setStatus("ANSWERING");
        es.setStartTime(now);
        examStudentMapper.updateById(es);

        // 更新考试状态
        if ("NOT_START".equals(exam.getStatus())) {
            exam.setStatus("IN_PROGRESS");
            this.updateById(exam);
        }

        // 获取题目列表
        List<ExamQuestion> eqList = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId)
                        .orderByAsc(ExamQuestion::getQuestionOrder)
        );

        if (exam.getIsRandomOrder() == 1) {
            Collections.shuffle(eqList);
        }

        return eqList.stream().map(eq -> {
            Question q = questionService.getById(eq.getQuestionId());
            if (q == null) return null;
            QuestionDTO dto = new QuestionDTO();
            dto.setId(q.getId());
            dto.setSubjectId(q.getSubjectId());
            dto.setQuestionType(q.getQuestionType());
            dto.setTitle(q.getTitle());
            dto.setDifficulty(q.getDifficulty());
            dto.setScore(eq.getScore());
            // 选择题填充选项
            if (isChoiceType(q.getQuestionType())) {
                List<QuestionOption> options = questionOptionMapper.selectList(
                        new LambdaQueryWrapper<QuestionOption>()
                                .eq(QuestionOption::getQuestionId, q.getId())
                                .orderByAsc(QuestionOption::getSortOrder)
                );
                dto.setOptions(options);
            }
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 提交单个题目答案（实时保存）
     */
    @Transactional
    public void saveAnswer(Long examId, Long studentId, Long questionId, String answerContent) {
        StudentAnswer sa = studentAnswerMapper.findOne(examId, studentId, questionId);
        if (sa == null) {
            sa = new StudentAnswer();
            sa.setExamId(examId);
            sa.setStudentId(studentId);
            sa.setQuestionId(questionId);
            Question q = questionService.getById(questionId);
            sa.setAnswerType(q != null ? q.getQuestionType() : "single_choice");
            sa.setAnswerContent(answerContent);
            sa.setIsCorrect(-1);
            sa.setScore(0);
            studentAnswerMapper.insert(sa);
        } else {
            sa.setAnswerContent(answerContent);
            sa.setUpdateTime(LocalDateTime.now());
            studentAnswerMapper.updateById(sa);
        }
    }

    /**
     * 防切屏记录
     */
    @Transactional
    public int recordCheat(Long examId, Long studentId) {
        ExamStudent es = examStudentMapper.selectOne(
                new LambdaQueryWrapper<ExamStudent>()
                        .eq(ExamStudent::getExamId, examId)
                        .eq(ExamStudent::getStudentId, studentId)
        );
        if (es == null) throw new IllegalArgumentException("考生记录不存在");

        int newCount = (es.getCheatCount() == null ? 0 : es.getCheatCount()) + 1;
        es.setCheatCount(newCount);

        Exam exam = this.getById(examId);
        if (exam.getMaxCheatCount() != null && newCount >= exam.getMaxCheatCount()) {
            // 强制交卷
            es.setStatus("FINISHED");
            es.setSubmitTime(LocalDateTime.now());
        }
        examStudentMapper.updateById(es);
        return newCount;
    }

    /**
     * 提交考试（计算成绩）
     */
    @Transactional
    public Map<String, Object> submitExam(Long examId, Long studentId) {
        Exam exam = this.getById(examId);

        ExamStudent es = examStudentMapper.selectOne(
                new LambdaQueryWrapper<ExamStudent>()
                        .eq(ExamStudent::getExamId, examId)
                        .eq(ExamStudent::getStudentId, studentId)
        );
        if (es == null) throw new IllegalArgumentException("考生记录不存在");
        es.setStatus("FINISHED");
        es.setSubmitTime(LocalDateTime.now());
        examStudentMapper.updateById(es);

        // 自动评分客观题
        List<StudentAnswer> answers = studentAnswerMapper.findByExamAndStudent(examId, studentId);
        int objectiveScore = 0;
        for (StudentAnswer sa : answers) {
            Question q = questionService.getById(sa.getQuestionId());
            if (q == null || sa.getAnswerContent() == null) continue;
            if (isChoiceType(q.getQuestionType())) {
                boolean correct = checkChoiceAnswer(q.getId(), sa.getAnswerContent());
                sa.setIsCorrect(correct ? 1 : 0);
                ExamQuestion eq = examQuestionMapper.selectOne(
                        new LambdaQueryWrapper<ExamQuestion>()
                                .eq(ExamQuestion::getExamId, examId)
                                .eq(ExamQuestion::getQuestionId, sa.getQuestionId())
                );
                int qScore = eq != null ? eq.getScore() : q.getScore();
                sa.setScore(correct ? qScore : 0);
                if (correct) objectiveScore += qScore;
                studentAnswerMapper.updateById(sa);
            }
        }

        // 保存考试成绩
        ExamScore score = examScoreMapper.selectOne(
                new LambdaQueryWrapper<ExamScore>()
                        .eq(ExamScore::getExamId, examId)
                        .eq(ExamScore::getStudentId, studentId)
        );
        if (score == null) {
            score = new ExamScore();
            score.setExamId(examId);
            score.setStudentId(studentId);
        }
        score.setObjectiveScore(objectiveScore);
        score.setTotalScore(objectiveScore); // 主观题等待批阅
        score.setIsPassed(objectiveScore >= exam.getPassScore() ? 1 : 0);
        if (score.getId() != null) {
            examScoreMapper.updateById(score);
        } else {
            examScoreMapper.insert(score);
        }

        // 更新考生总分
        es.setTotalScore(objectiveScore);
        es.setIsMarked(0); // 待批阅主观题
        examStudentMapper.updateById(es);

        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", objectiveScore);
        result.put("isPassed", objectiveScore >= exam.getPassScore());
        result.put("objectiveScore", objectiveScore);
        return result;
    }

    private boolean checkChoiceAnswer(Long questionId, String studentAnswer) {
        if (studentAnswer == null) return false;

        List<QuestionOption> options = questionOptionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, questionId)
                        .orderByAsc(QuestionOption::getSortOrder)
        );
        if (options == null || options.isEmpty()) return false;

        Set<String> correctLabels = options.stream()
                .filter(o -> o.getIsCorrect() == 1)
                .map(QuestionOption::getOptionLabel)
                .collect(Collectors.toSet());

        Set<String> studentLabels = new HashSet<>(Arrays.asList(studentAnswer.trim().split(",")));

        return correctLabels.equals(studentLabels);
    }

    private boolean isChoiceType(String type) {
        return "single_choice".equals(type) || "multi_choice".equals(type) || "true_false".equals(type);
    }

    private void copyProps(Exam src, ExamDTO dst) {
        dst.setId(src.getId());
        dst.setExamName(src.getExamName());
        dst.setSubjectId(src.getSubjectId());
        dst.setDescription(src.getDescription());
        dst.setStartTime(src.getStartTime());
        dst.setEndTime(src.getEndTime());
        dst.setDuration(src.getDuration());
        dst.setTotalScore(src.getTotalScore());
        dst.setPassScore(src.getPassScore());
        dst.setStatus(src.getStatus());
        dst.setIsRandomOrder(src.getIsRandomOrder());
        dst.setAllowRetry(src.getAllowRetry());
        dst.setMaxCheatCount(src.getMaxCheatCount());
        dst.setCreateBy(src.getCreateBy());
        dst.setCreateTime(src.getCreateTime());
    }
}
