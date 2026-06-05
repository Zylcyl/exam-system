package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("exam_score")
public class ExamScore implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Integer totalScore;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private Integer isPassed;
    private Integer rankPosition;
    private LocalDateTime createTime;
}
