package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamStudent;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExamStudentMapper extends BaseMapper<ExamStudent> {

    @Delete("DELETE FROM exam_student WHERE exam_id = #{examId}")
    int deleteByExamId(@Param("examId") Long examId);
}
