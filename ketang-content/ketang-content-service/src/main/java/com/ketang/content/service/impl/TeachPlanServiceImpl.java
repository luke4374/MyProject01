package com.ketang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ketang.content.mapper.TeachplanMapper;
import com.ketang.content.service.TeachPlanService;
import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;
import com.ketang.model.po.Teachplan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> queryTeachPlanTree(Long courseId) {
        List<TeachPlanDto> teachPlans = teachplanMapper.selectTreeNodes(courseId);
        return teachPlans;
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto) {
        if (saveTeachPlanDto.getId() == null){
            // 新增
            Teachplan newPlan = new Teachplan();
            BeanUtils.copyProperties(saveTeachPlanDto, newPlan);
            // 处理排序字段
            LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
            // select * from teachplan where courseid = {courseid} and parentid = parentid;
            lqw.eq(Teachplan::getParentid, newPlan.getParentid()).eq(Teachplan::getCourseId, newPlan.getCourseId());
            Integer count = teachplanMapper.selectCount(lqw);
            newPlan.setOrderby(count+1);
            teachplanMapper.insert(newPlan);
        }else{
           // 修改
            Teachplan teachplan = teachplanMapper.selectById(saveTeachPlanDto.getId());
            BeanUtils.copyProperties(saveTeachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
         }
    }
}
