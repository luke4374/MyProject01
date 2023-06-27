package com.ketang.content.api;

import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.model.dto.AddCourseDto;
import com.ketang.model.dto.CourseBaseInfoDto;
import com.ketang.model.dto.QueryCourseParamDto;
import com.ketang.model.po.CourseBase;
import com.ketang.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto addCourseDto){
        Long CompanyId = 1232141425L;
        return courseBaseService.createCourseBase(CompanyId, addCourseDto);
    }
}
