package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("exam_question")
public class ExamQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long questionId;
    private Integer questionOrder;
    private Integer score;
}
