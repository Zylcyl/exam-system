package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("question_option")
public class QuestionOption implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String optionLabel;
    private String optionContent;
    private Integer isCorrect;
    private Integer sortOrder;
}
