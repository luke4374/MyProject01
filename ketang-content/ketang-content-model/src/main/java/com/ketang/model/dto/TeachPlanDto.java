package com.ketang.model.dto;

import com.ketang.model.po.Teachplan;
import com.ketang.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * Dto数据传输对象
 */
@Data
public class TeachPlanDto extends Teachplan {
    // 子章节
    private List<Teachplan> teachPlanTreeNodes;

    // 媒资信息
    private TeachplanMedia teachplanMedia;
}
