package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.StudentAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StudentAnswerMapper extends BaseMapper<StudentAnswer> {

    @Select("SELECT * FROM student_answer WHERE exam_id = #{examId} AND student_id = #{studentId}")
    List<StudentAnswer> findByExamAndStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Select("SELECT * FROM student_answer WHERE exam_id = #{examId} AND student_id = #{studentId} AND question_id = #{questionId}")
    StudentAnswer findOne(@Param("examId") Long examId, @Param("studentId") Long studentId, @Param("questionId") Long questionId);
}
