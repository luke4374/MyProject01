package com.ketang.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 页面返回值model
 */
@Data
@AllArgsConstructor
@ToString
public class PageResult<T> implements Serializable {
    //返回json列表
    private List<T> items;

    //总记录数
    private long counts;

    //当前页码
    private long page;

    //每页记录数
    private long pageSize;

}
