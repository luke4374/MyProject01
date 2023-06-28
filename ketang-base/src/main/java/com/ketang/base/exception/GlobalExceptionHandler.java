package com.ketang.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * JSR303异常抛出捕获
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e){

        BindingResult bindingResult = e.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item->{
            errors.add(item.getDefaultMessage());
        });
        // 拼接信息
        String errorMsg = StringUtils.join(errors, ", ");
        // 记录异常log
        log.error("系统异常{}", e.getMessage(), errorMsg);
        // 返回前端
        return new RestErrorResponse(errorMsg);
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
