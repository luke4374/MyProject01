package com.ketang.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ketang.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author luke
 */
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {
    public List<CourseTeacher> selectByCourseId(Long courseId);
}
