package com.exam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.exam.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {
    private String examName;
    private Long subjectId;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer totalScore;
    private Integer passScore;
    private String status;
    private Integer isRandomOrder;
    private Integer allowRetry;
    private Integer maxCheatCount;
    private Long createBy;
    @TableLogic
    private Integer deleted;
}
