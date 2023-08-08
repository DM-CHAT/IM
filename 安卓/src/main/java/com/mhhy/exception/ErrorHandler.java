package com.mhhy.exception;



import com.mhhy.common.BaseResult;
import com.mhhy.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    public BaseResult<?> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        ResultCodeEnum resultCodeEnum = ResultCodeEnum.SERVER_EXCEPTION;
        return new BaseResult<>(resultCodeEnum.getCode(), resultCodeEnum.getRemark());
    }

    @ResponseBody
    @ExceptionHandler(OdcException.class)
    public BaseResult<?> handleException(OdcException e) {
        //log.debug("{}: {}, {}", e.getCode(), e.getResultCodeEnum().getRemark(), e.getOtherInfo());
        //String resultEnumMsg = MessageUtil.getResultMsg(e.getCode());
        BaseResult<Map<String, ?>> result = new BaseResult<>(e.getCode(), e.getMessage());
        result.setData(e.getOtherInfo());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResult<?> handleException(IllegalArgumentException e) {
        e.printStackTrace();
        int code = ResultCodeEnum.BAD_REQUEST.getCode();
        return new BaseResult<>(code, e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<?> handleException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        int code = ResultCodeEnum.BAD_REQUEST.getCode();
        return new BaseResult<>(code, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
