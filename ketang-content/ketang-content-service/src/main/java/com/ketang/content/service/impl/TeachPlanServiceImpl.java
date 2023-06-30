package com.ketang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ketang.base.exception.CommonError;
import com.ketang.base.exception.KeTangException;
import com.ketang.content.mapper.CourseTeacherMapper;
import com.ketang.content.mapper.TeachplanMapper;
import com.ketang.content.mapper.TeachplanMediaMapper;
import com.ketang.content.service.TeachPlanService;
import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;
import com.ketang.model.po.CourseTeacher;
import com.ketang.model.po.Teachplan;
import com.ketang.model.po.TeachplanMedia;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

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
            Integer count = getOrderCount(newPlan);
            newPlan.setOrderby(count+1);
            teachplanMapper.insert(newPlan);
        }else{
           // 修改
            Teachplan teachplan = teachplanMapper.selectById(saveTeachPlanDto.getId());
            BeanUtils.copyProperties(saveTeachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
         }
    }

    private Integer getOrderCount(Teachplan newPlan) {
        // 处理排序字段
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        Long parentid = newPlan.getParentid();
        Long courseId = newPlan.getCourseId();
        // select * from teachplan where courseid = {courseid} and parentid = parentid;
        lqw.eq(Teachplan::getParentid, parentid).eq(Teachplan::getCourseId, courseId);
        return teachplanMapper.selectCount(lqw);
    }

    @Override
    @Transactional
    public void deleteTeachPlan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getParentid() == 0){
            // 若删除的是大章节, 查询其是否有关联的子章节
            LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Teachplan::getParentid, id);
            Integer count = teachplanMapper.selectCount(lqw);
            if (count > 0) KeTangException.throwExp("120409", CommonError.TEACHPLAN_DELETE_ERROR);
            teachplanMapper.deleteById(id);
        }else {
            // 若删除小章节，需要同时删除media中与teachplan_id关联的信息
            teachplanMapper.deleteById(id);
            LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
            lqw.eq(TeachplanMedia::getTeachplanId, id);
            teachplanMediaMapper.delete(lqw);
        }
    }

    @Override
    @Transactional
    public void moveTeachplanUp(Long id) {
        Teachplan target = teachplanMapper.selectById(id);
        Integer tar_orderNum = target.getOrderby();
        // 检查是否为最上方
        if (tar_orderNum == 1) KeTangException.throwExp("已经到最上边了");
        // 获取前一条信息
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Teachplan::getCourseId, target.getCourseId())
           .eq(Teachplan::getParentid, target.getParentid())
           .eq(Teachplan::getOrderby, tar_orderNum - 1);
        Teachplan pre = teachplanMapper.selectOne(lqw);
        // 将前一条信息的orderby改为目标的order值
        pre.setOrderby(tar_orderNum);
        teachplanMapper.updateById(pre);
        // 改变目标order值
        target.setOrderby(tar_orderNum - 1);
        teachplanMapper.updateById(target);
    }

    @Override
    @Transactional
    public void moveTeachplanDown(Long id) {
        Teachplan target = teachplanMapper.selectById(id);
        Integer tar_orderNum = target.getOrderby();
        Integer orderCount = getOrderCount(target);
        // 检查是否为最上方
        if (tar_orderNum.equals(orderCount)) KeTangException.throwExp("已经到最下边了");
        // 获取后一条信息
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Teachplan::getCourseId, target.getCourseId())
           .eq(Teachplan::getParentid, target.getParentid())
           .eq(Teachplan::getOrderby, tar_orderNum + 1);
        Teachplan pre = teachplanMapper.selectOne(lqw);
        // 将前一条信息的orderby改为目标的order值
        pre.setOrderby(tar_orderNum);
        teachplanMapper.updateById(pre);
        // 改变目标order值
        target.setOrderby(tar_orderNum + 1);
        teachplanMapper.updateById(target);
    }

    @Override
    public List<CourseTeacher> queryTeachers(Long courseId) {
        return courseTeacherMapper.selectByCourseId(courseId);
    }

    @Override
    public CourseTeacher saveTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();
        if (id == null){
            // 新增教师
            CourseTeacher newTeacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher, newTeacher);
            newTeacher.setCreateDate(LocalDateTime.now());
            // insert成功newTeacher的id自动被赋值
            int result = courseTeacherMapper.insert(newTeacher);
            if (result <= 0) KeTangException.throwExp("教师新增模块数据库插入失败");
            return courseTeacherMapper.selectById(newTeacher.getId());
        }else {
           // 修改教师
            CourseTeacher teacherInfo = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacher, teacherInfo);
            int updateResult = courseTeacherMapper.updateById(teacherInfo);
            if (updateResult <= 0) KeTangException.throwExp("教师信息修改失败");
            return courseTeacherMapper.selectById(id);
        }
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        int delete = courseTeacherMapper.delete(lqw);
        if (delete <= 0) KeTangException.throwExp("教师删除失败！");
    }

}
