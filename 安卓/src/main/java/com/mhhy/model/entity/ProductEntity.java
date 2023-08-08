package com.mhhy.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("product01")
public class ProductEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userId;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private int state;

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("endTime", endTime);
        json.put("state", state);
        return json;
    }

}
