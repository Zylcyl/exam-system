package com.exam.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import com.exam.entity.Subject;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.QuestionOptionMapper;
import com.exam.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {

    private final QuestionOptionMapper optionMapper;
    private final SubjectMapper subjectMapper;

    public Page<QuestionDTO> pageQuestions(int page, int size, Long subjectId, String questionType, Integer difficulty, String keyword) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (StrUtil.isNotBlank(questionType)) {
            wrapper.eq(Question::getQuestionType, questionType);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Question::getTitle, keyword);
        }
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> pageResult = this.page(new Page<>(page, size), wrapper);

        // 转换为 DTO 并填充选项
        List<QuestionDTO> dtoList = pageResult.getRecords().stream().map(q -> {
            QuestionDTO dto = new QuestionDTO();
            copyProps(q, dto);
            // 填充科目名称
            Subject subject = subjectMapper.selectById(q.getSubjectId());
            if (subject != null) {
                dto.setSubjectName(subject.getSubjectName());
            }
            // 选择题填充选项
            if (isChoiceType(q.getQuestionType())) {
                List<QuestionOption> options = optionMapper.selectList(
                        new LambdaQueryWrapper<QuestionOption>()
                                .eq(QuestionOption::getQuestionId, q.getId())
                                .orderByAsc(QuestionOption::getSortOrder)
                );
                dto.setOptions(options);
            }
            return dto;
        }).collect(Collectors.toList());

        Page<QuestionDTO> dtoPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Transactional
    public void createQuestion(QuestionDTO dto) {
        this.save(dto);
        saveOptions(dto.getId(), dto.getQuestionType(), dto.getOptions());
    }

    @Transactional
    public void updateQuestion(QuestionDTO dto) {
        this.updateById(dto);
        // 删除旧选项，插入新选项
        optionMapper.deleteByQuestionId(dto.getId());
        saveOptions(dto.getId(), dto.getQuestionType(), dto.getOptions());
    }

    @Transactional
    public void deleteQuestion(Long id) {
        optionMapper.deleteByQuestionId(id);
        this.removeById(id);
    }

    private void saveOptions(Long questionId, String questionType, List<QuestionOption> options) {
        if (!isChoiceType(questionType) || options == null || options.isEmpty()) return;

        for (int i = 0; i < options.size(); i++) {
            QuestionOption opt = options.get(i);
            opt.setId(null);
            opt.setQuestionId(questionId);
            opt.setSortOrder(i);
            optionMapper.insert(opt);
        }
    }

    @Transactional
    public Map<String, Integer> importQuestions(MultipartFile file, Long subjectId, Long createBy) {
        int success = 0, fail = 0;
        try (InputStream is = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(is);
            reader.addHeaderAlias("题型", "questionType");
            reader.addHeaderAlias("题目内容", "title");
            reader.addHeaderAlias("难度", "difficulty");
            reader.addHeaderAlias("分值", "score");
            reader.addHeaderAlias("选项A", "optionA");
            reader.addHeaderAlias("选项B", "optionB");
            reader.addHeaderAlias("选项C", "optionC");
            reader.addHeaderAlias("选项D", "optionD");
            reader.addHeaderAlias("正确答案", "correctAnswer");
            reader.addHeaderAlias("解析", "analysis");

            List<Map<String, Object>> rows = reader.readAll();
            for (Map<String, Object> row : rows) {
                try {
                    String type = getString(row, "题型");
                    String title = getString(row, "题目内容");
                    if (StrUtil.isBlank(title)) { fail++; continue; }

                    Question q = new Question();
                    q.setSubjectId(subjectId);
                    q.setQuestionType(mapQuestionType(type));
                    q.setTitle(title);
                    q.setDifficulty(mapDifficulty(getString(row, "难度")));
                    q.setScore(getInt(row, "分值", 5));
                    q.setAnalysis(getString(row, "解析"));
                    q.setCreateBy(createBy);
                    this.save(q);

                    // 处理选项
                    String qType = q.getQuestionType();
                    if (isChoiceType(qType)) {
                        String correct = getString(row, "正确答案");
                        String[] labels = {"A", "B", "C", "D"};
                        for (int i = 0; i < labels.length; i++) {
                            String content = getString(row, "选项" + labels[i]);
                            if (StrUtil.isBlank(content)) continue;
                            QuestionOption opt = new QuestionOption();
                            opt.setQuestionId(q.getId());
                            opt.setOptionLabel(labels[i]);
                            opt.setOptionContent(content);
                            opt.setIsCorrect(correct != null && correct.contains(labels[i]) ? 1 : 0);
                            opt.setSortOrder(i);
                            optionMapper.insert(opt);
                        }
                    }
                    success++;
                } catch (Exception e) {
                    fail++;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel 解析失败: " + e.getMessage());
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return result;
    }

    private boolean isChoiceType(String type) {
        return "single_choice".equals(type) || "multi_choice".equals(type) || "true_false".equals(type);
    }

    private String mapQuestionType(String type) {
        if (type == null) return "single_choice";
        return switch (type.trim()) {
            case "单选题" -> "single_choice";
            case "多选题" -> "multi_choice";
            case "判断题" -> "true_false";
            case "填空题" -> "fill_blank";
            case "简答题" -> "short_answer";
            case "文件上传题" -> "file_upload";
            case "编程题" -> "coding";
            default -> "single_choice";
        };
    }

    private int mapDifficulty(String d) {
        if (d == null) return 3;
        return switch (d.trim()) {
            case "简单" -> 1;
            case "中等" -> 2;
            case "困难" -> 3;
            default -> {
                try { yield Integer.parseInt(d.trim()); }
                catch (NumberFormatException e) { yield 3; }
            }
        };
    }

    private String getString(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return val == null ? "" : val.toString().trim();
    }

    private int getInt(Map<String, Object> row, String key, int defaultVal) {
        try {
            Object val = row.get(key);
            return val == null ? defaultVal : Integer.parseInt(val.toString().trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void copyProps(Question src, QuestionDTO dst) {
        dst.setId(src.getId());
        dst.setSubjectId(src.getSubjectId());
        dst.setQuestionType(src.getQuestionType());
        dst.setTitle(src.getTitle());
        dst.setDifficulty(src.getDifficulty());
        dst.setScore(src.getScore());
        dst.setAnalysis(src.getAnalysis());
        dst.setKnowledgePoints(src.getKnowledgePoints());
        dst.setStatus(src.getStatus());
        dst.setCreateBy(src.getCreateBy());
        dst.setCreateTime(src.getCreateTime());
        dst.setUpdateTime(src.getUpdateTime());
    }
}
