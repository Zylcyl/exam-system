package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("student_answer")
public class StudentAnswer implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Long questionId;
    private String answerType;
    private String answerContent;
    private String answerFileIds;
    private Integer isCorrect;
    private Integer score;
    private Long markedBy;
    private String markComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
