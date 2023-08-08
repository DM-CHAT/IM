package com.mhhy.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BasePageReq {

    //最后一条数据的id
    //private int lastId;

    //分页页码，从1开始
    @ApiModelProperty(value = "分页页码，从1开始")
    private int page = 1;

    //分页大小，默认10
    @ApiModelProperty(value = "分页大小，默认10")
    private int size = 10;

    //获取page对象，用于mybatis-plus
    public IPage<?> getIPage() {
        return new Page<>(page, size).setSearchCount(false);
    }

    //获取offset，用于手动处理sql
    public int getOffset() {
        return page > 1 ? page * size - size : 0;
    }
}
