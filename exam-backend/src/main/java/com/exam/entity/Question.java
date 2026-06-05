package com.exam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.exam.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {
    private Long subjectId;
    private String questionType;
    private String title;
    private Integer difficulty;
    private Integer score;
    private String analysis;
    private String knowledgePoints;
    private Integer status;
    private Long createBy;
    @TableLogic
    private Integer deleted;
}
