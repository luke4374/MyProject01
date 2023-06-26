package com.ketang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.content.mapper.CourseBaseMapper;
import com.ketang.content.service.CourseBaseService;
import com.ketang.model.dto.QueryCourseParamDto;
import com.ketang.model.po.CourseBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CourseBaseServiceImpl implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseList(PageParams pageParams, QueryCourseParamDto courseParamDto) {
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        // 模糊查询
        lqw.like(StringUtils.isNotEmpty(courseParamDto.getCourseName()), CourseBase::getName, courseParamDto.getCourseName());
        // 审核状态
        lqw.eq(StringUtils.isNotEmpty(courseParamDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamDto.getAuditStatus());
        // 添加课程状态查询
        lqw.eq(StringUtils.isNotEmpty(courseParamDto.getPublishStatus()), CourseBase::getStatus, courseParamDto.getPublishStatus());
//        lqw.eq();


        //分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, lqw);
        // 封装PageResult
        List<CourseBase> items = pageResult.getRecords();
        // 总记录数
        long total = pageResult.getTotal();

        PageResult<CourseBase> finalResult = new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        return finalResult;
    }
}
