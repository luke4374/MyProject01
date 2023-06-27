package com.ketang.content.service;

import com.ketang.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {

    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
