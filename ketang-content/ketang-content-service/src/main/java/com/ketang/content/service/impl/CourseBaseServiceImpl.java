package com.ketang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ketang.base.exception.KeTangException;
import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.content.mapper.CourseBaseMapper;
import com.ketang.content.mapper.CourseCategoryMapper;
import com.ketang.content.mapper.CourseMarketMapper;
import com.ketang.content.service.CourseBaseService;
import com.ketang.model.dto.AddCourseDto;
import com.ketang.model.dto.CourseBaseInfoDto;
import com.ketang.model.dto.QueryCourseParamDto;
import com.ketang.model.po.CourseBase;
import com.ketang.model.po.CourseCategory;
import com.ketang.model.po.CourseMarket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourseBaseServiceImpl implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

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

    @Transactional// 增删改加事务管理
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 合法性校验
//        if(StringUtils.isBlank(addCourseDto.getName())) KeTangException.throwExp("课程名为空");
//        if(StringUtils.isBlank(addCourseDto.getMt())) KeTangException.throwExp("课程分类为空");
//        if(StringUtils.isBlank(addCourseDto.getSt())) KeTangException.throwExp("课程小分类为空");
        // 向课程基本信息表和营销表写数据
        // Write to CourseBase
        CourseBase courseBase = new CourseBase();
        /*
        从参数中获取数据封装进数据库PO类方式繁杂，使用BeanUtils方式封装
            courseBase.setName(addCourseDto.getName());
         */
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        // 审核状态默认值
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        // 插入数据库
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0) throw new RuntimeException("课程插入失败");
        // Write to CourseMarket
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);

        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);
        // 调用校验保存方法
        saveCourseMarketInfo(courseMarket);
        // 数据库查询封装
        CourseBaseInfoDto courseInfo = getCourseInfo(courseId);

        return courseInfo;
    }

    private CourseBaseInfoDto getCourseInfo(Long id){
        // 从数据库查询两表中的数据
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null) return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null) return null;
        // 重新封装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        // 获取分类名称
        CourseCategory Category_getMtName_Tmp = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        CourseCategory Category_getStName_Tmp = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        // 赋值
        courseBaseInfoDto.setMtName(Category_getMtName_Tmp.getName());
        courseBaseInfoDto.setStName(Category_getStName_Tmp.getName());
        return courseBaseInfoDto;
    }

    /**
     * 营销信息保存与校验，存在则更新，不存在则添加
     * @param CMNew
     * @return
     */
    private int saveCourseMarketInfo(CourseMarket CMNew){
        // 合法校验
        String charge = CMNew.getCharge();
        if (StringUtils.isEmpty(charge)){
            KeTangException.throwExp("收费为空");
        }else if(charge.equals("201001")){
            if (CMNew.getPrice() == null || CMNew.getPrice().floatValue() <= 0 || CMNew.getOriginalPrice() <= 0){
                KeTangException.throwExp("标记收费,但收费价格不合规");
            }
        }

        Long id = CMNew.getId();
        CourseMarket courseMarketRes = courseMarketMapper.selectById(id);
        if (courseMarketRes == null){
            return courseMarketMapper.insert(CMNew);
        }else {
            // 将CMNew拷贝到获取到的对象中
            BeanUtils.copyProperties(CMNew, courseMarketRes);
            CMNew.setId(CMNew.getId());
            return courseMarketMapper.updateById(courseMarketRes);
        }
    }

}
