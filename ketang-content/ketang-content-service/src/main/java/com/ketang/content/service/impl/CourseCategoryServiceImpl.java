package com.ketang.content.service.impl;

import com.ketang.content.mapper.CourseCategoryMapper;
import com.ketang.content.service.CourseCategoryService;
import com.ketang.model.dto.CourseCategoryTreeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> categoryDtos = courseCategoryMapper.selectTreeNodes(id);
        // Dto list转map处理数据
        Map<String, CourseCategoryTreeDto> map = categoryDtos.stream()
                .filter(item -> !id.equals(item.getId())) // 过滤根节点
                .collect(Collectors.toMap(
                key -> key.getId(),
                value -> value,
                (oldkey, newkey) -> newkey //当有重复的key插入时以新插入的为准
        ));
        // 封装树形节点
        List<CourseCategoryTreeDto> finalCategoryTreeDtos = new ArrayList<>();
        // 流遍历处理
        categoryDtos.stream().filter(item -> !id.equals(item.getId()))
                .forEach(item->{
                    if (item.getParentid().equals(id)){
                        finalCategoryTreeDtos.add(item);
                    }
                    // 找到当前处理节点的父节点数据
                    CourseCategoryTreeDto parentNode = map.get(item.getParentid());
                    // 当父节点不为空时
                    if (parentNode != null){
                        // 若该节点子节点为空, 新建list
                        if(parentNode.getChildrenTreeNodes() == null){
                            parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        // 插入当前item
                        parentNode.getChildrenTreeNodes().add(item);
                    }

                });

        return finalCategoryTreeDtos;
    }
}
