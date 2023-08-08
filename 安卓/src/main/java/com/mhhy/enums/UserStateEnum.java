package com.mhhy.enums;

public enum UserStateEnum {

    ENABLE(0, "未启用"),
    NORMAL(1, "正常"),
    DISABLE(2, "禁用")
    ;




    private final int code;
    private final String remark;

    UserStateEnum(int code, String remark) {
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
