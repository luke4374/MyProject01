package com.ketang.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@ControllerAdvice
//@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 自定义异常捕获
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(KeTangException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse myException(KeTangException e){
        // 记录异常log
        log.error("系统异常{}", e.getErrMessage(), e);
        // 返回前端
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse SystemException(Exception e){
        // 记录异常log
        log.error("系统异常{}", e.getMessage(), e);
        // 返回前端
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}
