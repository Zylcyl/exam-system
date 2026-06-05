package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.QuestionOption;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionOptionMapper extends BaseMapper<QuestionOption> {

    @Delete("DELETE FROM question_option WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
