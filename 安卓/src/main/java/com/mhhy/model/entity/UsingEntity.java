package com.mhhy.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_using")
public class UsingEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    private String node;

    private int state;
}
