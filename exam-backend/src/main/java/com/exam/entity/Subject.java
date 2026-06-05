package com.exam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.exam.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subject")
public class Subject extends BaseEntity {
    private String subjectName;
    private Long parentId;
    private String description;
    private Integer sortOrder;
    private Integer status;
    @TableLogic
    private Integer deleted;
}
