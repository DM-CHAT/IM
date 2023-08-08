package com.mhhy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum  RegexEnum {

    PHONE("^1\\d{10}$"),
    EMAIL("^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$"),
    // 密码长度8-20位且至少包含大写字母、小写字母、数字或特殊符号中的任意三种
    PASSWORD("^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,20}$");

    private String regx;

    RegexEnum(String regx) {
        this.regx = regx;
    }

    public boolean isMatcher(String val){
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(val);
        return m.matches();
    }

    public String getRegx() {
        return regx;
    }
}
