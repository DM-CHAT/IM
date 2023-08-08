package com.mhhy.enums;

public enum ResultCodeEnum {

    SUCCESS(0, "success"),//请求成功
    BAD_REQUEST(400, "错误请求"),//错误请求
    UN_AUTHORIZATION(401, "请登录"),//该钱包已在其他设备登录
    NOT_REPEAT_SEND_SMS(402, "请勿重发短信"),
    SERVER_EXCEPTION(500, "网络异常"),//网络异常
    BALANCE_NOT_ENOUGH(600, "余额不足"),//网络异常
    PAY_ORDER(700, "支付失败"),//网络异常
    DATA_ORDER(800, "数据异常"),//网络异常

    ACCOUNT_ALREADY(1000, "user already exist"),
    PASSWORD_ERROR(1001, "wrong password"),
    ACCOUNT_NOT_EXIST(1002, "User not exist"),
    IM_REGISTER_FAILED(1003, "register im account failed."),
    DB_ERROR(1004, "db error."),
    ;




    private final int code;
    private final String remark;

    ResultCodeEnum(int code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public int getCode() {
        return code;
    }

    public String getRemark() {
        return remark;
    }
}
