package com.ketang.content.service;

import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;
import com.ketang.model.po.CourseTeacher;

import java.util.List;

public interface TeachPlanService {
    public List<TeachPlanDto> queryTeachPlanTree(Long courseId);
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);
    public void deleteTeachPlan(Long id);
    public void moveTeachplanUp(Long id);
    public void moveTeachplanDown(Long id);
    public List<CourseTeacher> queryTeachers(Long courseId);
    public CourseTeacher saveTeacher(CourseTeacher courseTeacher);
    public void deleteTeacher(Long courseId, Long teacherId);
}
