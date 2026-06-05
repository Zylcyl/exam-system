package com.exam.dto;

import com.exam.entity.Question;
import com.exam.entity.QuestionOption;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionDTO extends Question {
    private List<QuestionOption> options;
    private String subjectName;

    // 用于接收前端的答案（选择题多选用数组）
    private List<String> answerList;
    private String answerText;
}
