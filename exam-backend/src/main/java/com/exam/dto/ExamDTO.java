package com.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDTO {
    private Long id;
    private String examName;
    private Long subjectId;
    private String subjectName;
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
    private List<Long> questionIds;
    private List<Long> studentIds;
    private List<QuestionScore> questionScores;
    private Integer questionCount;
    private Integer studentCount;
    private LocalDateTime createTime;
}

@Data
class QuestionScore {
    private Long questionId;
    private Integer score;
}
