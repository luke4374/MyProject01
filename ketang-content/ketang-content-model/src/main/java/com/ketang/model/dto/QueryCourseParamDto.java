package com.ketang.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * http查询条件参数
 */
@Data
@ToString
public class QueryCourseParamDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}
