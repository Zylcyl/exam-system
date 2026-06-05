package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("exam_student")
public class ExamStudent implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer totalScore;
    private Integer cheatCount;
    private Integer isMarked;
}
