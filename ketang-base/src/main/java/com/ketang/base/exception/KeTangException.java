package com.ketang.base.exception;


/**
 * 自定义异常类型
 */
public class KeTangException extends RuntimeException{
    private String errMessage;

    public KeTangException() {
    }

    public KeTangException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public static void throwExp(String msg){
        throw new KeTangException(msg);
    }

    public static void throwExp(CommonError error){
        throw new KeTangException(error.getErrMessage());
    }
}
