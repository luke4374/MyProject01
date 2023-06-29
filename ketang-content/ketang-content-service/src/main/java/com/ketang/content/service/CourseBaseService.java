package com.ketang.content.service;

import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.model.dto.AddCourseDto;
import com.ketang.model.dto.CourseBaseInfoDto;
import com.ketang.model.dto.EditCourseDto;
import com.ketang.model.dto.QueryCourseParamDto;
import com.ketang.model.po.CourseBase;

public interface CourseBaseService {

    /**
     * 课程分页查询
     * @param pageParams 分页查询参数
     * @param courseParamDto 查询条件
     * @return
     */
    public PageResult<CourseBase> queryCourseList(PageParams pageParams, QueryCourseParamDto courseParamDto);

    /**
     * 课程新增
     * @param CompanyId 机构id
     * @param addCourseDto 添加信息
     * @return
     */
    public CourseBaseInfoDto createCourseBase(Long CompanyId, AddCourseDto addCourseDto);

    /**
     * 根据id查询课程
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto queryById(Long courseId);

    /**
     * 根据id修改课程
     * @param courseId
     * @param addCourseDto
     * @return
     */
    public CourseBaseInfoDto updateCourse(Long companyId, EditCourseDto editCourseDto);
}
