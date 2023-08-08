package com.mhhy.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("node")
public class NodeEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String ip;

    private String region;

    private int state;

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("IP", ip);
        json.put("region", region);
        json.put("state", state);
        return json;
    }

}
