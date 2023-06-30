package com.ketang.content.api;

import com.ketang.base.model.PageResult;
import com.ketang.content.service.TeachPlanService;
import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;
import com.ketang.model.po.CourseTeacher;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api("课程计划")
@RestController
public class TeachPlanController {

    @Autowired
    TeachPlanService teachPlanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.queryTeachPlanTree(courseId);
    }

    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody @Validated SaveTeachPlanDto saveTeachPlanDto){
        teachPlanService.saveTeachPlan(saveTeachPlanDto);
    }

    /**
     * 根据课程计划id来删除
     * @param id 章节id
     */
    @DeleteMapping("/teachplan/{id}")
    public void deletePlan(@PathVariable Long id){
        teachPlanService.deleteTeachPlan(id);
    }

    @PostMapping("/teachplan/moveup/{id}")
    public void moveUp(@PathVariable Long id){
        teachPlanService.moveTeachplanUp(id);
    }
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDown(@PathVariable Long id){
        teachPlanService.moveTeachplanDown(id);
    }
    // 教师管理
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> queryAllTeachers(@PathVariable Long courseId){
        return teachPlanService.queryTeachers(courseId);
    }

    @PostMapping("/courseTeacher")
    public CourseTeacher saveTeacher(@RequestBody CourseTeacher courseTeacher){
        return teachPlanService.saveTeacher(courseTeacher);
    }

    @PutMapping("/courseTeacher")
    public CourseTeacher updateTeacher(@RequestBody CourseTeacher courseTeacher){
        return teachPlanService.saveTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long teacherId){
        teachPlanService.deleteTeacher(courseId, teacherId);
    }
}
