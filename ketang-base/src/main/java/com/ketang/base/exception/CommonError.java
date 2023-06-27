package com.ketang.base.exception;

public enum CommonError {
    UNKNOWN_ERROR("执行出现错误，请重试"),
    PARAM_ERROR("参数有误"),
    NULL_OBJECT("对象为空"),
    QUERY_NULL("查询结果为空"),
    REQUEST_NULL("请求参数为空");

    private String errMessage;

    CommonError(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
