package com.mhhy.enums;

public interface BaseEnum {

    int getEnumCode();

    //BaseEnum valueOf(int enumCode);

    default String i18nKey() {
        return null;
    }
}
