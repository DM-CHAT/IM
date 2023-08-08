package com.mhhy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("t_dapp")
public class DappEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String dappId;

    private String name;

    private String dappInfo;

    private Integer state = 1;
}
