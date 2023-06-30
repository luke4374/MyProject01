package com.ketang.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 定义异常交互格式
 */
@Data
public class RestErrorResponse implements Serializable {

    private String errCode;
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public RestErrorResponse(String errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }
}
