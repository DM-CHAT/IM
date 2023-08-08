package com.mhhy.exception;

import com.mhhy.enums.ResultCodeEnum;
import lombok.Data;

import java.util.Map;

@Data
public class OdcException extends RuntimeException {

    private int code;

    private ResultCodeEnum resultCodeEnum;

    private Map<String, ?> otherInfo;

    public OdcException(ResultCodeEnum codeEnum) {
        super(codeEnum.getRemark());
        code = codeEnum.getCode();
        resultCodeEnum = codeEnum;
    }

    public OdcException(ResultCodeEnum codeEnum, Map<String, ?> otherInfo) {
        super(codeEnum.getRemark());
        this.code = codeEnum.getCode();
        this.otherInfo = otherInfo;
    }

    public int getCode() {
        return code;
    }

    public ResultCodeEnum getResultCodeEnum() {
        return resultCodeEnum;
    }
}