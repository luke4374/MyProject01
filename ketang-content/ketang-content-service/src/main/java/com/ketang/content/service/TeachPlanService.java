package com.ketang.content.service;

import com.ketang.model.dto.SaveTeachPlanDto;
import com.ketang.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
    public List<TeachPlanDto> queryTeachPlanTree(Long courseId);
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);
}
