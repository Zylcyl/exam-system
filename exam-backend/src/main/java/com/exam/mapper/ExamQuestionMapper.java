package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamQuestion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    @Delete("DELETE FROM exam_question WHERE exam_id = #{examId}")
    int deleteByExamId(@Param("examId") Long examId);
}
