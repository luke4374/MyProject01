package com.ketang.content.api;

import com.ketang.base.exception.ValidationGroups;
import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.model.dto.AddCourseDto;
import com.ketang.model.dto.CourseBaseInfoDto;
import com.ketang.model.dto.EditCourseDto;
import com.ketang.model.dto.QueryCourseParamDto;
import com.ketang.model.po.CourseBase;
import com.ketang.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseService courseBaseService;

    @ApiOperation("课程信息查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamDto courseParamDto){
        PageResult<CourseBase> courseBasePageResult = courseBaseService.queryCourseList(pageParams, courseParamDto);
        return courseBasePageResult;
    }

    @ApiOperation("课程添加信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody
                                              @Validated(ValidationGroups.Insert.class) // 激活JSR 新增分组的校验
                                              AddCourseDto addCourseDto){
        Long CompanyId = 1232141425L;
        return courseBaseService.createCourseBase(CompanyId, addCourseDto);
    }

    @ApiOperation("按ID查询课程")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto queryById(@PathVariable Long courseId){
        return courseBaseService.queryById(courseId);
    }

    @ApiOperation("根据id修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourse(@RequestBody
                                          @Validated(ValidationGroups.Update.class)
                                          EditCourseDto editCourseDto){
        Long CompanyId = 1232141425L;
        return courseBaseService.updateCourse(CompanyId, editCourseDto);
    }
}
