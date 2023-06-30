package com.ketang.base.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 自定义异常类型
 */
@Data
public class KeTangException extends RuntimeException{
    private String errCode;
    private String errMessage;

    public KeTangException() {
    }

    public KeTangException(String message) {
        super(message);
        this.errMessage = message;
    }

    public KeTangException(String errCode, String errMessage){
        super(errMessage);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public static void throwExp(String msg){
        throw new KeTangException(msg);
    }

    public static void throwExp(String code, String msg){
        throw new KeTangException(code, msg);
    }
    public static void throwExp(String code, CommonError error){
        throw new KeTangException(code, error.getErrMessage());
    }
    public static void throwExp(CommonError error){
        throw new KeTangException(error.getErrMessage());
    }
}
