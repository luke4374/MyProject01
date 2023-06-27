package com.ketang.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ketang.model.dto.CourseCategoryTreeDto;
import com.ketang.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author luke
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    public List<CourseCategoryTreeDto> selectTreeNodes(String id);

}
