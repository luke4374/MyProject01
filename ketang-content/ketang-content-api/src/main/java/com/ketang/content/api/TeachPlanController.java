package com.ketang.content.api;

import com.ketang.content.service.TeachPlanService;
import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;
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
}
