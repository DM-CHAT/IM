package com.mhhy.enums;

public enum UserTypeEnum {

    ADMIN(1, "管理员"),
    CUSTOMER(2, "客服"),
    NORMAL(3, "普通用户")
    ;




    private final int code;
    private final String remark;

    UserTypeEnum(int code, String remark) {
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
